package net.friedl.fling.service;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
import net.friedl.fling.model.dto.AuthCodeDto;
import net.friedl.fling.model.dto.FlingDto;
import net.friedl.fling.model.mapper.AuthCodeMapper;
import net.friedl.fling.model.mapper.FlingMapper;
import net.friedl.fling.persistence.entities.FlingEntity;
import net.friedl.fling.persistence.repositories.FlingRepository;

@Slf4j
@Service
@Transactional
public class FlingService {
    private FlingRepository flingRepository;

    private FlingMapper flingMapper;

    private AuthCodeMapper authCodeMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public FlingService(FlingRepository flingRepository, FlingMapper flingMapper, AuthCodeMapper authCodeMapper) {
        this.flingRepository = flingRepository;
        this.flingMapper = flingMapper;
        this.authCodeMapper = authCodeMapper;
    }

    public List<FlingDto> findAll() {
        return flingMapper.map(flingRepository.findAll());
    }

    public void createFling(FlingDto flingDto) {
        if (!StringUtils.hasText(flingDto.getShareUrl())) {
            flingDto.setShareUrl(generateShareUrl());
        }

        var flingEntity = flingMapper.map(flingDto);
        flingRepository.save(flingEntity);
    }

    public Boolean existsShareUrl(String shareUrl) {
        return !flingRepository.findByShareUrl(shareUrl).isEmpty();
    }

    public void mergeFling(Long flingId, FlingDto flingDto) {
        var flingEntity = flingRepository.getOne(flingId);

        mergeNonEmpty(flingDto::getAllowUpload, flingEntity::setAllowUpload);
        mergeNonEmpty(flingDto::getDirectDownload, flingEntity::setDirectDownload);
        mergeNonEmpty(flingDto::getExpirationClicks, flingEntity::setExpirationClicks);
        mergeNonEmpty(flingDto::getExpirationTime, flingEntity::setExpirationTime);
        mergeNonEmpty(flingDto::getName, flingEntity::setName);
        mergeNonEmpty(flingDto::getShared, flingEntity::setShared);
        mergeNonEmpty(flingDto::getShareUrl, flingEntity::setShareUrl);
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
        return flingRepository.getOne(flingId).getAuthCodes()
                .stream().anyMatch(ae -> ae.getAuthCode().equals(authCode));
    }

    public void protect(Long flingId, AuthCodeDto authCodeDto) {
        var fling = flingRepository.getOne(flingId);
        var authCode = authCodeMapper.map(authCodeDto);

        authCode.setFling(fling);
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

    public static String generateShareUrl() {
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

    private <T> void mergeNonEmpty(Supplier<T> sup, Consumer<T> con) {
        T r = sup.get();
        if(r != null) con.accept(r);
    }
}
