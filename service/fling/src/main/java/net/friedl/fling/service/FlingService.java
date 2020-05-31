package net.friedl.fling.service;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
import net.friedl.fling.model.dto.FlingDto;
import net.friedl.fling.model.mapper.FlingMapper;
import net.friedl.fling.persistence.archive.Archive;
import net.friedl.fling.persistence.archive.ArchiveException;
import net.friedl.fling.persistence.entities.ArtifactEntity;
import net.friedl.fling.persistence.entities.FlingEntity;
import net.friedl.fling.persistence.repositories.FlingRepository;

@Slf4j
@Service
@Transactional
public class FlingService {

    private FlingRepository flingRepository;
    private FlingMapper flingMapper;
    private Archive archive;
    private MessageDigest keyHashDigest;

    @Autowired
    public FlingService(FlingRepository flingRepository, FlingMapper flingMapper, Archive archive, MessageDigest keyHashDigest) {
        this.flingRepository = flingRepository;
        this.flingMapper = flingMapper;
        this.archive = archive;
        this.keyHashDigest = keyHashDigest;
    }

    public List<FlingDto> findAll() {
        return flingMapper.map(flingRepository.findAll());
    }

    public Long createFling(FlingDto flingDto) {
        if (!StringUtils.hasText(flingDto.getShareUrl())) {
            flingDto.setShareUrl(generateShareUrl());
        }

        var flingEntity = flingMapper.map(flingDto);
        flingEntity.setAuthCode(hashKey(flingEntity.getAuthCode()));
        flingEntity = flingRepository.save(flingEntity);
        return flingEntity.getId();
    }

    public Boolean existsShareUrl(String shareUrl) {
        return !flingRepository.findByShareUrl(shareUrl).isEmpty();
    }

    public void mergeFling(Long flingId, FlingDto flingDto) {
        var flingEntity = flingRepository.getOne(flingId);

        mergeNonEmpty(flingDto::getAllowUpload, flingEntity::setAllowUpload);
        mergeNonEmpty(flingDto::getDirectDownload, flingEntity::setDirectDownload);
        mergeWithEmpty(flingDto::getExpirationClicks, flingEntity::setExpirationClicks);
        mergeWithEmpty(flingDto::getExpirationTime, flingEntity::setExpirationTime);
        mergeNonEmpty(flingDto::getName, flingEntity::setName);
        mergeNonEmpty(flingDto::getShared, flingEntity::setShared);
        mergeNonEmpty(flingDto::getShareUrl, flingEntity::setShareUrl);
        mergeWithEmpty(() -> hashKey(flingDto.getAuthCode()), flingEntity::setAuthCode);
    }

    public Optional<FlingDto> findFlingById(Long flingId) {
        return flingMapper.map(flingRepository.findById(flingId));
    }

    public Optional<FlingDto> findFlingByShareId(String shareUrl) {
        return flingMapper.map(flingRepository.findByShareUrl(shareUrl));
    }

    public void deleteFlingById(Long flingId) {
        flingRepository.deleteById(flingId);
    }

    public boolean hasAuthCode(Long flingId, String authCode) {
        var fling = flingRepository.getOne(flingId);

        if(!StringUtils.hasText(fling.getAuthCode())) return true;

        return fling.getAuthCode().equals(hashKey(authCode));
    }

    public String getShareName(String shareUrl) {

        FlingEntity flingEntity = flingRepository.findByShareUrl(shareUrl).orElseThrow();

        if (flingEntity.getArtifacts().size() > 1)
            return flingEntity.getName();
        else if (flingEntity.getArtifacts().size() == 1)
            return flingEntity.getArtifacts().stream().findFirst().get().getName();

        return null;
    }

    public Long countArtifacts(Long flingId) {
        return flingRepository.countArtifactsById(flingId);
    }

    public Long getFlingSize(Long flingId) {
        var fling = flingRepository.getOne(flingId);

        return fling.getArtifacts().stream()
                .map(ae -> ae.getSize())
                .reduce(0L, (acc, as) -> acc+as);
    }

    public String packageFling(Long flingId) throws IOException, ArchiveException {
        var fling = flingRepository.getOne(flingId);
        var tempFile = Files.createTempFile(Long.toString(flingId), ".zip");

        try(var zipStream = new ZipOutputStream(new FileOutputStream(tempFile.toFile()))){
            zipStream.setLevel(Deflater.BEST_SPEED);
            for(ArtifactEntity artifactEntity: fling.getArtifacts()) {
                ZipEntry ze = new ZipEntry(artifactEntity.getName());
                zipStream.putNextEntry(ze);

                var artifactStream = archive.get(artifactEntity.getDoi());
                try(var archiveEntryStream = new BufferedInputStream(artifactStream)) {
                    int b;
                    while( (b = archiveEntryStream.read()) != -1 ) {
                        zipStream.write(b);
                    }
                } finally {
                    zipStream.closeEntry();
                }
            }
        }

        return tempFile.getFileName().toString();
    }

    public Pair<InputStream, Long> downloadFling(String fileId) throws IOException, ArchiveException {
        var tempFile = Paths.get(System.getProperty("java.io.tmpdir"), fileId).toFile();

        var archiveLength = tempFile.length();
        var archiveStream =  new FileInputStream(tempFile);

        return Pair.of(archiveStream, archiveLength);
    }

    public String generateShareUrl() {
        var key = KeyGenerators
                .secureRandom(16)
                .generateKey();

        return Base64.getUrlEncoder().encodeToString(key)
                // replace all special chars [=-_] in RFC 4648
                // "URL and Filename safe" table with characters from
                // [A-Za-z0-9]. Hence, the generated share url will only consist
                // of [A-Za-z0-9].
                .replace('=', 'q')
                .replace('_', 'u')
                .replace('-', 'd');
    }

    public String hashKey(String key) {
        if(!StringUtils.hasText(key)) return null;

        return new String(Hex.encode(keyHashDigest.digest(key.getBytes())));
    }

    private <T> void mergeNonEmpty(Supplier<T> sup, Consumer<T> con) {
        T r = sup.get();
        if(r != null) con.accept(r);
    }

    private <T> void mergeWithEmpty(Supplier<T> sup, Consumer<T> con) {
        T r = sup.get();
        con.accept(r);
    }
}
