package net.friedl.fling.service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;

import net.friedl.fling.model.dto.ArtifactDto;
import net.friedl.fling.model.mapper.ArtifactMapper;
import net.friedl.fling.persistence.archive.Archive;
import net.friedl.fling.persistence.archive.ArchiveException;
import net.friedl.fling.persistence.entities.ArtifactEntity;
import net.friedl.fling.persistence.repositories.ArtifactRepository;
import net.friedl.fling.persistence.repositories.FlingRepository;

@Service
@Transactional
public class ArtifactService {

    private FlingRepository flingRepository;
    private ArtifactRepository artifactRepository;
    private ArtifactMapper artifactMapper;
    private Archive archive;

    @Autowired
    public ArtifactService(ArtifactRepository artifactRepository, FlingRepository flingRepository,
            ArtifactMapper artifactMapper, Archive archive) {
        this.artifactRepository = artifactRepository;
        this.flingRepository = flingRepository;
        this.artifactMapper = artifactMapper;
        this.archive = archive;
    }

    public List<ArtifactDto> findAllArtifacts(Long flingId) {
        return artifactMapper.map(artifactRepository.findAllByFlingId(flingId));
    }

    public ArtifactDto storeArtifact(Long flingId, InputStream artifact) throws ArchiveException {
        var flingEntity = flingRepository.findById(flingId).orElseThrow();
        var archiveId = archive.store(artifact);

        ArtifactEntity artifactEntity = new ArtifactEntity();
        artifactEntity.setDoi(archiveId);
        artifactEntity.setFling(flingEntity);

        artifactRepository.save(artifactEntity);

        return artifactMapper.map(artifactEntity);
    }

    public Optional<ArtifactDto> findArtifact(Long artifactId) {
        return artifactMapper.map(artifactRepository.findById(artifactId));
    }

    public ArtifactDto mergeArtifact(Long artifactId, String body) {
        JsonParser jsonParser = JsonParserFactory.getJsonParser();
        Map<String, Object> parsedBody = jsonParser.parseMap(body);

        artifactRepository.findById(artifactId)
                // map entity to dto
                .map(artifactMapper::map)
                // merge parsedBody into dto
                .map(a -> artifactMapper.merge(a, parsedBody))
                // map dto to entity
                .map(artifactMapper::map)
                .ifPresent(artifactRepository::save);

        return artifactMapper.map(artifactRepository.getOne(artifactId));
    }

    public void deleteArtifact(Long artifactId) throws ArchiveException {
        var doi = artifactRepository.getOne(artifactId).getDoi();
        artifactRepository.deleteById(artifactId);
        archive.remove(doi);
    }

    public String generateDownloadId(Long artifactId) {
        // TODO: This id is not secured! Generate temporary download id
        return artifactRepository.getOne(artifactId).getDoi();
    }

    public InputStream downloadArtifact(String downloadId) throws ArchiveException {
        return archive.get(downloadId);
    }
}
