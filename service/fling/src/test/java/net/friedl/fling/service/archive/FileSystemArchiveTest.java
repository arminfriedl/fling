package net.friedl.fling.service.archive;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import net.friedl.fling.persistence.entities.ArtifactEntity;
import net.friedl.fling.persistence.entities.FlingEntity;
import net.friedl.fling.persistence.repositories.ArtifactRepository;
import net.friedl.fling.service.archive.impl.FileSystemArchive;

@ExtendWith(SpringExtension.class)
public class FileSystemArchiveTest {
  @Autowired
  private FileSystemArchive fileSystemArchive;

  @MockBean
  private ArtifactRepository artifactRepository;

  private FlingEntity flingEntity1;

  private FlingEntity flingEntity2;

  private ArtifactEntity artifactEntity1;

  private ArtifactEntity artifactEntity2;

  @TempDir
  static Path tempDir;

  @TestConfiguration
  static class FlingServiceTestConfiguration {
    @Bean
    public FileSystemArchive fileSystemArchive(ArtifactRepository artifactRepository)
        throws URISyntaxException {
      FileSystemArchive fileSystemArchive = new FileSystemArchive(artifactRepository);
      fileSystemArchive.setArchivePath(tempDir);
      return fileSystemArchive;
    }
  }

  @BeforeEach
  public void beforeEach() throws IOException, URISyntaxException {
    repopulateArchivePath();
    setupTestEntites();
  }

  @Test
  public void getArtifact_flingDiskForFlingIdDoesNotExist_throws() {
    flingEntity1.setId(UUID.randomUUID());
    artifactEntity1.setFling(flingEntity1);
    when(artifactRepository.getOne(artifactEntity1.getId())).thenReturn(artifactEntity1);

    assertThrows(IOException.class, () -> fileSystemArchive.getArtifact(artifactEntity1.getId()));
  }

  @Test
  public void getArtifact_returnsArtifact() throws IOException {
    when(artifactRepository.getOne(artifactEntity1.getId())).thenReturn(artifactEntity1);

    InputStream expectedArtifact =
        getClass().getClassLoader().getResourceAsStream("filesystem/artifacts/artifact1");
    byte[] expectedArtifactData = expectedArtifact.readAllBytes();
    expectedArtifact.close();

    InputStream retrievedArtifact = fileSystemArchive.getArtifact(artifactEntity1.getId());
    byte[] retrievedArtifactData = retrievedArtifact.readAllBytes();
    retrievedArtifact.close();

    assertThat(retrievedArtifactData, equalTo(expectedArtifactData));
  }

  @Test
  public void getFling_doesNotExist_throws() {
    assertThrows(IOException.class, () -> fileSystemArchive.getFling(UUID.randomUUID()));
  }

  @Test
  public void getFling_returnsFling() throws IOException {
    UUID flingUUID = new UUID(0, 0);
    InputStream expectedFling = getClass().getClassLoader()
        .getResourceAsStream("filesystem/archive_path/" + flingUUID.toString() + ".zip");
    byte[] expectedFlingData = expectedFling.readAllBytes();
    expectedFling.close();

    InputStream retrievedFling = fileSystemArchive.getFling(flingUUID);
    byte[] retrievedFlingData = retrievedFling.readAllBytes();
    retrievedFling.close();

    assertThat(retrievedFlingData, equalTo(expectedFlingData));
  }

  @Test
  public void deleteArtifact_setsArchivedFalse() throws IOException {
    when(artifactRepository.getOne(artifactEntity1.getId())).thenReturn(artifactEntity1);

    fileSystemArchive.deleteArtifact(artifactEntity1.getId());

    assertThat(artifactEntity1.getArchived(), equalTo(false));

    InputStream flingStream =
        new FileInputStream(
            tempDir.resolve(artifactEntity1.getFling().getId().toString() + ".zip").toFile());
    ZipInputStream zis = new ZipInputStream(flingStream);
    ZipEntry zipEntry;
    while ((zipEntry = zis.getNextEntry()) != null) {
      assertThat(zipEntry.getName(), not(equalTo(artifactEntity1.getPath().toString())));
      zis.closeEntry();
    }
    zis.close();
  }

  @Test
  public void deleteArtifact_deletesArtifactFromZipDisk() throws IOException {
    when(artifactRepository.getOne(artifactEntity1.getId())).thenReturn(artifactEntity1);

    fileSystemArchive.deleteArtifact(artifactEntity1.getId());

    InputStream flingStream =
        new FileInputStream(
            tempDir.resolve(artifactEntity1.getFling().getId().toString() + ".zip").toFile());
    ZipInputStream zis = new ZipInputStream(flingStream);
    ZipEntry zipEntry;
    while ((zipEntry = zis.getNextEntry()) != null) {
      assertThat(zipEntry.getName(), not(equalTo(artifactEntity1.getPath().toString())));
      zis.closeEntry();
    }
    zis.close();
  }

  @Test
  public void storeArtifact_setsArchivedTrue() throws IOException, URISyntaxException {
    InputStream artifact2Stream = new FileInputStream(
        new File(
            getClass().getClassLoader().getResource("filesystem/artifacts/artifact2").toURI()));
    when(artifactRepository.getOne(artifactEntity2.getId())).thenReturn(artifactEntity2);

    fileSystemArchive.storeArtifact(artifactEntity2.getId(), artifact2Stream);

    artifact2Stream.close();

    assertThat(artifactEntity2.getArchived(), equalTo(true));
  }

  @Test
  public void storeArtifact_storesArtifactToFlingDisk() throws URISyntaxException, IOException {
    InputStream artifact2Stream = new FileInputStream(
        new File(
            getClass().getClassLoader().getResource("filesystem/artifacts/artifact2").toURI()));
    when(artifactRepository.getOne(artifactEntity2.getId())).thenReturn(artifactEntity2);

    fileSystemArchive.storeArtifact(artifactEntity2.getId(), artifact2Stream);

    artifact2Stream.close();

    InputStream flingStream =
        new FileInputStream(
            tempDir.resolve(artifactEntity2.getFling().getId().toString() + ".zip").toFile());
    ZipInputStream zis = new ZipInputStream(flingStream);
    ZipEntry zipEntry;
    List<String> diskEntries = new LinkedList<>();
    while ((zipEntry = zis.getNextEntry()) != null) {
      diskEntries.add(zipEntry.getName());
      zis.closeEntry();
    }
    zis.close();

    assertThat(diskEntries, hasItem(Path.of("/").relativize(artifactEntity2.getPath()).toString()));
  }

  @Test
  public void deleteFling_setsArchivedFalseForAllContainedArtifacts() throws IOException {
    when(artifactRepository.findAllByFlingId(artifactEntity1.getFling().getId()))
        .thenReturn(List.of(artifactEntity1));

    fileSystemArchive.deleteFling(artifactEntity1.getFling().getId());

    assertThat(artifactEntity1.getArchived(), equalTo(false));
  }

  @Test
  public void deleteFling_deletesZipDisk() throws IOException {
    assertThat(Files.exists(tempDir.resolve(artifactEntity1.getFling().getId() + ".zip")),
        equalTo(true));

    fileSystemArchive.deleteFling(artifactEntity1.getFling().getId());

    assertThat(Files.exists(tempDir.resolve(artifactEntity1.getFling().getId() + ".zip")),
        equalTo(false));
  }

  @Test
  public void deleteFling_zipDiskNotFound_noThrow() throws IOException {
    assertThat(Files.exists(tempDir.resolve(artifactEntity2.getFling().getId() + ".zip")),
        equalTo(false));

    assertDoesNotThrow(() -> fileSystemArchive.deleteFling(artifactEntity2.getFling().getId()));
  }

  private void repopulateArchivePath() throws IOException, URISyntaxException {
    Files.walkFileTree(tempDir, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
          throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException e)
          throws IOException {
        if (e == null) {
          Files.delete(dir);
          return FileVisitResult.CONTINUE;
        } else {
          // directory iteration failed
          throw e;
        }
      }
    });

    Path source =
        Path.of(getClass().getClassLoader().getResource("filesystem/archive_path").toURI());
    Path target = tempDir;
    Files.walkFileTree(source, Set.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
              throws IOException {
            Path targetdir = target.resolve(source.relativize(dir));
            try {
              Files.copy(dir, targetdir);
            } catch (FileAlreadyExistsException e) {
              if (!Files.isDirectory(targetdir))
                throw e;
            }
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            Files.copy(file, target.resolve(source.relativize(file)));
            return FileVisitResult.CONTINUE;
          }
        });
  }

  private void setupTestEntites() {
    // Fling1/Artifact1
    this.artifactEntity1 = new ArtifactEntity();
    artifactEntity1.setId(UUID.randomUUID());
    artifactEntity1.setPath(Path.of("artifact1"));
    artifactEntity1.setArchived(true);

    this.flingEntity1 = new FlingEntity();
    flingEntity1.setId(new UUID(0, 0));
    flingEntity1.setName("fling1");

    artifactEntity1.setFling(flingEntity1);


    // Fling2/Artifact2
    this.artifactEntity2 = new ArtifactEntity();
    artifactEntity2.setId(UUID.randomUUID());
    artifactEntity2.setPath(Path.of("/", "/sub", "artifact2"));
    artifactEntity2.setArchived(false);

    this.flingEntity2 = new FlingEntity();
    flingEntity2.setId(new UUID(1, 0));
    flingEntity2.setName("fling2");

    artifactEntity2.setFling(flingEntity2);
  }
}
