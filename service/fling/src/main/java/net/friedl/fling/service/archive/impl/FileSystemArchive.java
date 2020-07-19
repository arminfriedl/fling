package net.friedl.fling.service.archive.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.transaction.Transactional;
import javax.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import net.friedl.fling.persistence.entities.ArtifactEntity;
import net.friedl.fling.persistence.repositories.ArtifactRepository;
import net.friedl.fling.service.archive.ArchiveService;

@Slf4j
@Service
@ConfigurationProperties("fling.archive.filesystem")
@Transactional
public class FileSystemArchive implements ArchiveService {
  @NotBlank
  private Path archivePath;

  private ArtifactRepository artifactRepository;

  private Map<URI, FileSystem> filesystems;

  public FileSystemArchive(ArtifactRepository artifactRepository) {
    this.artifactRepository = artifactRepository;
    this.filesystems = new HashMap<>();
  }

  @PostConstruct
  public void postConstruct() {
    try {
      Files.createDirectories(archivePath);
      log.debug("Using archive path {}", archivePath);
    } catch (IOException e) {
      log.error("Could not create directory at archive path {}", archivePath);
      throw new UncheckedIOException(e);
    }
  }

  @PreDestroy
  public void preDestroy() {
    filesystems.forEach((uri, zfs) -> {
      try {
        zfs.close();
        log.debug("Closed {}", uri);
      } catch (IOException e) {
        log.error("Could not close file system for {}", uri);
      }
    });
  }

  @Override
  public InputStream getArtifact(UUID artifactId) throws IOException {
    log.debug("Reading data for artifact {}", artifactId);

    FileSystem zipDisk = getZipDisk(artifactId);
    return zipDisk.provider().newInputStream(getZipDiskPath(artifactId, zipDisk),
        StandardOpenOption.READ);

    // do not close zip disk here or the input stream will be closed as well
  }

  @Override
  public InputStream getFling(UUID flingId) throws IOException {
    log.debug("Reading data for fling {}", flingId);
    Path zipDiskPath = archivePath.resolve(flingId.toString() + ".zip");
    log.debug("Zip disk path is {}", zipDiskPath);
    return new FileInputStream(zipDiskPath.toFile());
  }

  @Override
  public void storeArtifact(UUID artifactId, InputStream artifactStream) throws IOException {
    log.debug("Storing artifact {}", artifactId);

    synchronized (filesystems) {
      setArchived(artifactId, false);
      FileSystem zipDisk = getZipDisk(artifactId);
      Files.copy(artifactStream, getZipDiskPath(artifactId, zipDisk),
          StandardCopyOption.REPLACE_EXISTING);

      // we need to close the zipDisk in order to flush it to disk
      closeZipDisk(artifactId);
      setArchived(artifactId, true);
    }
  }

  @Override
  public void deleteArtifact(UUID artifactId) throws IOException {
    log.debug("Deleting artifact {}", artifactId);
    FileSystem zipDisk = getZipDisk(artifactId);
    Files.delete(getZipDiskPath(artifactId, zipDisk));

    // we need to close the zipDisk in order to flush it to disk
    closeZipDisk(artifactId);
    setArchived(artifactId, false);
  }

  @Override
  public void deleteFling(UUID flingId) throws IOException {
    URI zipDiskUri = resolveFlingUri(flingId);

    log.debug("Closing zip disk at {}", zipDiskUri);

    // make sure nobody opens the filesystem while it is being closed and deleted
    synchronized (filesystems) {
      FileSystem zipDisk = filesystems.remove(zipDiskUri);

      if (zipDisk != null) {
        zipDisk.close();
        log.debug("Zip disk closed");
      } else {
        log.debug("No open zip disk found");
      }

      Path zipDiskPath = archivePath.resolve(flingId.toString() + ".zip");
      log.debug("Deleting fling [.id={}] at {}", flingId, zipDiskPath);
      if (Files.exists(zipDiskPath)) {
        Files.delete(zipDiskPath);
      } else {
        log.warn("No fling disk found at {}", zipDiskPath);
      }

      artifactRepository.findAllByFlingId(flingId).forEach(ar -> ar.setArchived(false));
    }
  }

  private void setArchived(UUID artifactId, boolean archived) {
    ArtifactEntity artifactEntity = artifactRepository.getOne(artifactId);
    artifactEntity.setArchived(archived);
    log.debug("Artifact[.id={}] set to {} archived", artifactId, archived ? "" : "not");
  }

  private Path getZipDiskPath(UUID artifactId, FileSystem zipDisk) {
    ArtifactEntity artifactEntity = artifactRepository.getOne(artifactId);
    log.debug("Getting zip disk path for {}", artifactEntity.getPath());

    Path zipDiskPath = zipDisk.getPath(artifactEntity.getPath().toString());
    if (zipDiskPath.getParent() != null && !Files.exists(zipDiskPath.getParent())) {
      try {
        Files.createDirectories(zipDiskPath.getParent());
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    log.debug("Got zip disk path {}", zipDiskPath);
    return zipDiskPath;
  }

  private FileSystem getZipDisk(UUID artifactId) throws IOException {
    log.debug("Retrieving zip disk for artifact {}", artifactId);
    URI uri = resolveArtifactUri(artifactId);
    log.debug("Looking for zip disk at uri {}", uri);

    // make sure nobody closes, deletes or interleavingly opens the filesystem while it is
    // being opened
    synchronized (filesystems) {
      if (!filesystems.containsKey(uri)) {
        log.debug("Zip disk does not exist. Creating zip disk for {}", uri);
        FileSystem zipDisk = FileSystems.newFileSystem(uri, Map.of("create", "true"));
        filesystems.put(uri, zipDisk);
      }

      return filesystems.get(uri);
    }
  }

  private void closeZipDisk(UUID artifactId) throws IOException {
    log.debug("Closing zip disk for artifact {}", artifactId);
    URI uri = resolveArtifactUri(artifactId);
    log.debug("Closing zip disk at uri {}", uri);

    // make sure nobody opens the filesystem while it is being closed
    synchronized (filesystems) {
      FileSystem zipDisk = filesystems.remove(uri);
      if (zipDisk == null) {
        log.warn("Could not close zip disk at {}. Filesystem not found.", uri);
        return;
      }

      zipDisk.close();
    }

  }

  private URI resolveArtifactUri(UUID artifactId) throws IOException {
    ArtifactEntity artifactEntity = artifactRepository.getOne(artifactId);
    UUID flingId = artifactEntity.getFling().getId();

    return resolveFlingUri(flingId);
  }

  private URI resolveFlingUri(UUID flingId) throws IOException {
    Path zipDiskPath = archivePath.resolve(flingId.toString() + ".zip");
    return URI.create("jar:file:" + zipDiskPath.toFile().getCanonicalPath());
  }

  public void setArchivePath(String archivePath) {
    this.archivePath = Paths.get(archivePath);
  }

  public void setArchivePath(Path archivePath) {
    this.archivePath = archivePath;
  }

}
