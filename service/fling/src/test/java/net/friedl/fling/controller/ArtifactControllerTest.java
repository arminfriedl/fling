package net.friedl.fling.controller;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.EntityNotFoundException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import net.friedl.fling.model.dto.ArtifactDto;
import net.friedl.fling.service.ArtifactService;
import net.friedl.fling.service.archive.ArchiveService;

@WebMvcTest(controllers = ArtifactController.class,
    // do auto-configure security
    excludeAutoConfiguration = SecurityAutoConfiguration.class,
    // do not try to create beans in security
    excludeFilters = @Filter(type = FilterType.REGEX, pattern = "net.friedl.fling.security.*"))
class ArtifactControllerTest {
  @Autowired
  private MockMvc mvc;

  @MockBean
  private ArtifactService artifactService;

  @MockBean
  private ArchiveService archiveService;

  private static final UUID ARTIFACT_ID = UUID.randomUUID();

  private ArtifactDto artifactDto =
      new ArtifactDto(ARTIFACT_ID, Path.of("testArtifact"), Instant.EPOCH, false);

  @Test
  public void getArtifact_noArtifactWithId_notFound() throws Exception {
    when(artifactService.getById(ARTIFACT_ID)).thenThrow(EntityNotFoundException.class);

    mvc.perform(get("/api/artifacts/{id}", ARTIFACT_ID))
        .andExpect(status().isNotFound());
  }

  @Test
  public void getArtifacts_ok() throws Exception {
    when(artifactService.getById(ARTIFACT_ID)).thenReturn(artifactDto);

    mvc.perform(get("/api/artifacts/{id}", ARTIFACT_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", equalTo(ARTIFACT_ID.toString())));
  }

  @Test
  public void deleteArtifact_noArtifactWithId_notFound() throws Exception {
    doThrow(EntityNotFoundException.class).when(artifactService).delete(ARTIFACT_ID);

    mvc.perform(delete("/api/artifacts/{id}", ARTIFACT_ID))
        .andExpect(status().isNotFound());
  }

  @Test
  public void deleteArtifact_ok() throws Exception {
    mvc.perform(delete("/api/artifacts/{id}", ARTIFACT_ID))
        .andExpect(status().isOk());
  }

  @Test
  public void uploadArtifact_ioError_serverError() throws Exception {
    doThrow(IOException.class).when(archiveService).storeArtifact(any(), any());

    byte[] payload = "Payload".getBytes();
    mvc.perform(post("/api/artifacts/{id}/data", ARTIFACT_ID)
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .content(payload))
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void uploadArtifact_ok() throws Exception {
    byte[] payload = "Payload".getBytes();
    mvc.perform(post("/api/artifacts/{id}/data", ARTIFACT_ID)
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .content(payload))
        .andExpect(status().isOk());
  }

  @Test
  public void downloadArtifact_noArtifact_notFound() throws Exception {
    doThrow(EntityNotFoundException.class).when(artifactService).getById(ARTIFACT_ID);

    mvc.perform(get("/api/artifacts/{id}/data", ARTIFACT_ID))
        .andExpect(header().doesNotExist(HttpHeaders.CONTENT_DISPOSITION))
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE,
            not(equalTo(MediaType.APPLICATION_OCTET_STREAM_VALUE))))
        .andExpect(status().isNotFound());
  }

  @Test
  public void downloadArtifact_ioError_serverError() throws Exception {
    doThrow(IOException.class).when(archiveService).getArtifact(ARTIFACT_ID);

    mvc.perform(get("/api/artifacts/{id}/data", ARTIFACT_ID))
        .andExpect(header().doesNotExist(HttpHeaders.CONTENT_DISPOSITION))
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE,
            not(equalTo(MediaType.APPLICATION_OCTET_STREAM_VALUE))))
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void downloadArtifact_ok() throws Exception {
    when(artifactService.getById(ARTIFACT_ID)).thenReturn(artifactDto);
    byte[] testData = "test".getBytes();
    when(archiveService.getArtifact(any())).thenReturn(new ByteArrayInputStream(testData));

    mvc.perform(get("/api/artifacts/{id}/data", ARTIFACT_ID))
        .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
        .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION))
        .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
            Matchers.containsString("attachment;filename")))
        .andExpect(content().bytes(testData));
  }
}
