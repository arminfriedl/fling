package net.friedl.fling.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import net.friedl.fling.model.dto.ArtifactDto;
import net.friedl.fling.model.mapper.ArtifactMapper;
import net.friedl.fling.model.mapper.ArtifactMapperImpl;
import net.friedl.fling.persistence.entities.ArtifactEntity;
import net.friedl.fling.persistence.entities.FlingEntity;
import net.friedl.fling.persistence.repositories.ArtifactRepository;
import net.friedl.fling.persistence.repositories.FlingRepository;
import net.friedl.fling.service.archive.ArchiveService;

@ExtendWith(SpringExtension.class)
public class ArtifactServiceTest {

  @Autowired
  private ArtifactService artifactService;

  @MockBean
  private FlingRepository flingRepository;

  @MockBean
  private ArtifactRepository artifactRepository;

  @MockBean
  private ArchiveService archiveService;

  private FlingEntity flingEntity;

  private ArtifactEntity artifactEntity1;

  private ArtifactEntity artifactEntity2;

  @TestConfiguration
  static class FlingServiceTestConfiguration {
    @Bean
    public ArtifactMapper artifactMapper() {
      return new ArtifactMapperImpl();
    }

    @Bean
    public ArtifactService artifactService(ArtifactRepository artifactRepository,
        FlingRepository flingRepository, ArtifactMapper artifactMapper,
        ArchiveService archiveService) {

      return new ArtifactService(artifactRepository, flingRepository, artifactMapper, archiveService);
    }
  }

  @BeforeEach
  public void beforeEach() {
    this.artifactEntity1 = new ArtifactEntity();
    artifactEntity1.setId(UUID.randomUUID());
    artifactEntity1.setUploadTime(Instant.EPOCH);
    artifactEntity1.setPath(Path.of("artifact1"));
    
    this.artifactEntity2 = new ArtifactEntity();
    artifactEntity2.setId(UUID.randomUUID());
    artifactEntity2.setUploadTime(Instant.EPOCH.plus(12000, ChronoUnit.DAYS));
    artifactEntity2.setPath(Path.of("/","/sub","artifact2"));
    
    this.flingEntity = new FlingEntity();
    flingEntity.setId(UUID.randomUUID());
    flingEntity.setName("fling");
    flingEntity.setCreationTime(Instant.now());

    when(flingRepository.save(any())).then(new Answer<FlingEntity>() {
      @Override
      public FlingEntity answer(InvocationOnMock invocation) throws Throwable {
        FlingEntity flingEntity = invocation.getArgument(0);
        if(flingEntity.getId() == null) flingEntity.setId(UUID.randomUUID());
        return flingEntity;
      }
    });
    
    when(artifactRepository.save(any())).then(new Answer<ArtifactEntity>() {
      @Override
      public ArtifactEntity answer(InvocationOnMock invocation) throws Throwable {
        ArtifactEntity artifactEntity = invocation.getArgument(0);
        artifactEntity.setId(UUID.randomUUID());
        return artifactEntity;
      }
    });

  }
  
  @Test
  public void getById_artifactExists_ok() {
    when(artifactRepository.getOne(artifactEntity1.getId())).thenReturn(artifactEntity1);
    
    ArtifactDto artifactDto = artifactService.getById(artifactEntity1.getId());
    assertThat(artifactDto.getId(), equalTo(artifactEntity1.getId()));
    assertThat(artifactDto.getPath(), equalTo(artifactEntity1.getPath()));
    assertThat(artifactDto.getUploadTime(), equalTo(artifactEntity1.getUploadTime()));
  }
  
  @Test
  public void create_createsArtifact_ok() {
    ArtifactDto artifactToCreate = ArtifactDto.builder()
        .uploadTime(Instant.now())
        .path(Path.of("new", "artifacts"))
        .build();

    ArtifactDto createdArtifact = artifactService.create(flingEntity.getId(), artifactToCreate);
    
    assertThat(createdArtifact.getUploadTime(), equalTo(artifactToCreate.getUploadTime()));
    assertThat(createdArtifact.getPath(), equalTo(artifactToCreate.getPath()));
  }
  
  @Test
  public void delete_deletesArchiveAndArtifactEntry() throws IOException {
    artifactService.delete(artifactEntity1.getId());
    
    verify(archiveService).deleteArtifact(artifactEntity1.getId());
    verify(artifactRepository).deleteById(artifactEntity1.getId());
  }

}
