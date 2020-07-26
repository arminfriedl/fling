package net.friedl.fling.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import net.friedl.fling.model.dto.ArtifactDto;
import net.friedl.fling.model.dto.FlingDto;
import net.friedl.fling.service.ArtifactService;
import net.friedl.fling.service.FlingService;
import net.friedl.fling.service.archive.ArchiveService;

@WebMvcTest(controllers = FlingController.class,
    // do auto-configure security
    excludeAutoConfiguration = SecurityAutoConfiguration.class,
    // do not try to create beans in security
    excludeFilters = @Filter(type = FilterType.REGEX, pattern = "net.friedl.fling.security.*"))
public class FlingControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper mapper;

  @MockBean
  private FlingService flingService;

  @MockBean
  private ArtifactService artifactService;

  @MockBean
  private ArchiveService archiveService;

  private static final UUID flingId = UUID.randomUUID();

  private FlingDto flingDto = new FlingDto(flingId, "name", Instant.EPOCH, "shareId", "authCode",
      false, true, true, 1, null);

  private ArtifactDto artifactDto =
      new ArtifactDto(UUID.randomUUID(), Path.of("testArtifact"), Instant.EPOCH, false);

  @Test
  public void getFlings_noFlings_empty() throws Exception {
    when(flingService.findAll()).thenReturn(List.of());

    mockMvc.perform(get("/api/fling"))
        .andExpect(jsonPath("$", hasSize(0)))
        .andExpect(status().isOk());
  }

  @Test
  public void getFlings_allFlings() throws Exception {
    when(flingService.findAll()).thenReturn(List.of(flingDto, flingDto));

    mockMvc.perform(get("/api/fling"))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id", equalTo(flingId.toString())))
        .andExpect(status().isOk());
  }

  @Test
  public void postFling_ok() throws Exception {
    mockMvc.perform(post("/api/fling")
        .content(mapper.writeValueAsString(flingDto))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void postFling_validatesBody_notOk() throws Exception {
    FlingDto invalidFlingDto = new FlingDto();

    mockMvc.perform(post("/api/fling")
        .content(mapper.writeValueAsString(invalidFlingDto))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void replaceFling_validatesBody_notOk() throws Exception {
    FlingDto invalidFlingDto = new FlingDto();

    mockMvc.perform(put("/api/fling/{id}", flingId)
        .content(mapper.writeValueAsString(invalidFlingDto))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void replaceFling_ok() throws Exception {
    FlingDto flingDto = new FlingDto(flingId, "new-name", Instant.EPOCH, "shareId", "new-authCode",
        false, true, true, 1, null);

    mockMvc.perform(put("/api/fling/{id}", flingId)
        .content(mapper.writeValueAsString(flingDto))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void postArtifact_ok() throws Exception {
    mockMvc.perform(post("/api/fling/{id}/artifacts", flingId)
        .content(mapper.writeValueAsString(artifactDto))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void postArtifact_validatesBody_notOk() throws Exception {
    ArtifactDto invalidArtifactDto = new ArtifactDto();

    mockMvc.perform(post("/api/fling/{id}/artifacts", flingId)
        .content(mapper.writeValueAsString(invalidArtifactDto))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void getArtifact_noFlingWithId_notFound() throws Exception {
    doThrow(EntityNotFoundException.class).when(flingService).getArtifacts(flingId);

    mockMvc.perform(get("/api/fling/{id}/artifacts", flingId))
        .andExpect(status().isNotFound());
  }

  @Test
  public void getArtifact_flingFound_noArtifacts_emptySet() throws Exception {
    when(flingService.getArtifacts(flingId)).thenReturn(Set.of());

    mockMvc.perform(get("/api/fling/{id}/artifacts", flingId))
        .andExpect(status().isOk())
        .andExpect(content().string(equalTo("[]")));
  }

  @Test
  public void getArtifact_flingFound_hasArtifacts_returnArtifacts() throws Exception {
    ArtifactDto artifactDto1 = ArtifactDto.builder()
        .id(new UUID(0, 0))
        .build();

    ArtifactDto artifactDto2 = ArtifactDto.builder()
        .id(new UUID(0, 1))
        .build();

    when(flingService.getArtifacts(flingId)).thenReturn(Set.of(artifactDto1, artifactDto2));

    mockMvc.perform(get("/api/fling/{id}/artifacts", flingId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id",
            anyOf(equalTo(new UUID(0, 0).toString()), equalTo(new UUID(0, 1).toString()))))
        .andExpect(jsonPath("$[1].id",
            anyOf(equalTo(new UUID(0, 0).toString()), equalTo(new UUID(0, 1).toString()))));
  }

  @Test
  public void getFling_noFlingWithId_notFound() throws Exception {
    doThrow(EntityNotFoundException.class).when(flingService).getById(flingId);

    mockMvc.perform(get("/api/fling/{id}", flingId))
        .andExpect(status().isNotFound());
  }

  @Test
  public void getFling_flingFound_returnsFling() throws Exception {
    when(flingService.getById(flingId)).thenReturn(flingDto);

    mockMvc.perform(get("/api/fling/{id}", flingId))
        .andExpect(jsonPath("$.id", equalTo(flingId.toString())))
        .andExpect(status().isOk());
  }

  @Test
  public void getFlingByShareId_noFlingWithShareId_notFound() throws Exception {
    doThrow(EntityNotFoundException.class).when(flingService).getByShareId("doesNotExist");

    mockMvc.perform(get("/api/fling/share/{shareId}", "doesNotExist"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void getFlingByShareId_flingFind_returnsFling() throws Exception {
    doReturn(flingDto).when(flingService).getByShareId("shareId");

    mockMvc.perform(get("/api/fling/share/{shareId}", "shareId"))
        .andExpect(jsonPath("$.id", equalTo(flingId.toString())))
        .andExpect(status().isOk());
  }

  @Test
  public void deleteFling_noFlingWithId_notFound() throws Exception {
    doThrow(EntityNotFoundException.class).when(flingService).delete(flingId);

    mockMvc.perform(delete("/api/fling/{id}", flingId))
        .andExpect(status().isNotFound());
  }

  @Test
  public void deleteFling_ok() throws Exception {
    doNothing().when(flingService).delete(flingId);

    mockMvc.perform(delete("/api/fling/{id}", flingId))
        .andExpect(status().isOk());
  }

  @Test
  public void getFlingData_ioError_serverError() throws Exception {
    doThrow(IOException.class).when(archiveService).getFling(flingId);

    mockMvc.perform(get("/api/fling/{id}/data", flingId))
        .andExpect(header().doesNotExist(HttpHeaders.CONTENT_DISPOSITION))
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE,
            not(equalTo(MediaType.APPLICATION_OCTET_STREAM_VALUE))))
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void getFlingData_ok() throws Exception {
    when(flingService.getById(flingId)).thenReturn(flingDto);
    int[] testZipInt = new int[] {
        0x50, 0x4b, 0x03, 0x04, 0x0a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x76, 0x77, 0xe4, 0x50, 0xc6,
        0x35,
        0xb9, 0x3b, 0x05, 0x00, 0x00, 0x00, 0x05, 0x00, 0x00, 0x00, 0x04, 0x00, 0x1c, 0x00, 0x74,
        0x65,
        0x73, 0x74, 0x55, 0x54, 0x09, 0x00, 0x03, 0x40, 0x7d, 0x00, 0x5f, 0x37, 0x7d, 0x00, 0x5f,
        0x75,
        0x78, 0x0b, 0x00, 0x01, 0x04, 0xe8, 0x03, 0x00, 0x00, 0x04, 0xe8, 0x03, 0x00, 0x00, 0x74,
        0x65,
        0x73, 0x74, 0x0a, 0x50, 0x4b, 0x01, 0x02, 0x1e, 0x03, 0x0a, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x76,
        0x77, 0xe4, 0x50, 0xc6, 0x35, 0xb9, 0x3b, 0x05, 0x00, 0x00, 0x00, 0x05, 0x00, 0x00, 0x00,
        0x04,
        0x00, 0x18, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0xb4, 0x81, 0x00, 0x00,
        0x00,
        0x00, 0x74, 0x65, 0x73, 0x74, 0x55, 0x54, 0x05, 0x00, 0x03, 0x40, 0x7d, 0x00, 0x5f, 0x75,
        0x78,
        0x0b, 0x00, 0x01, 0x04, 0xe8, 0x03, 0x00, 0x00, 0x04, 0xe8, 0x03, 0x00, 0x00, 0x50, 0x4b,
        0x05,
        0x06, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x4a, 0x00, 0x00, 0x00, 0x43, 0x00,
        0x00,
        0x00, 0x00, 0x00
    };
    byte[] testZip = new byte[testZipInt.length];
    for (int idx = 0; idx < testZip.length; idx++) testZip[idx] = (byte) testZipInt[idx];

    when(archiveService.getFling(any()))
        .thenAnswer((invocation) -> {
          // need to use thenAnswer here to always return a fresh new (unclosed) input stream
          return new ByteArrayInputStream(testZip);
        });

    mockMvc.perform(get("/api/fling/{id}/data", flingId))
        .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
        .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION))
        .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
            Matchers.containsString("attachment;filename")))
        .andExpect(content().bytes(testZip));
  }
}
