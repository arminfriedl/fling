package net.friedl.fling.controller;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.friedl.fling.model.dto.ArtifactDto;
import net.friedl.fling.service.ArtifactService;
import net.friedl.fling.service.archive.ArchiveService;

@Slf4j
@RestController
@RequestMapping("/api/artifacts")
@Tag(name = "artifact", description = "Operations on /api/artifacts")
public class ArtifactController {

  private ArtifactService artifactService;
  private ArchiveService archiveService;

  @Autowired
  public ArtifactController(ArtifactService artifactService, ArchiveService archiveService) {
    this.artifactService = artifactService;
    this.archiveService = archiveService;
  }

  @GetMapping(path = "/{id}")
  public ArtifactDto getArtifact(@PathVariable UUID id) {
    return artifactService.getById(id);
  }

  @DeleteMapping(path = "/{id}")
  public void deleteArtifact(@PathVariable UUID id) {
    artifactService.delete(id);
  }

  @Operation(requestBody = @RequestBody(
      content = @Content(schema = @Schema(type = "string", format = "binary"))))
  @PostMapping(path = "/{id}/data")
  public void uploadArtifactData(@PathVariable UUID id, HttpServletRequest request) {
    try {
      archiveService.storeArtifact(id, request.getInputStream());
    } catch (IOException e) {
      log.error("Could not read input from stream", e);
      throw new UncheckedIOException(e);
    }
  }

  @Operation(responses = {
      @ApiResponse(responseCode = "200", 
          content = @Content(
              mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
              schema = @Schema(type = "string", format = "binary")))
  })
  @GetMapping(path = "/{id}/data")
  public ResponseEntity<Resource> downloadArtifact(@PathVariable UUID id) {
    ArtifactDto artifactDto = artifactService.getById(id);
    InputStreamResource data = new InputStreamResource(archiveService.getArtifact(id));

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment;filename=\"" + artifactDto.getPath().getFileName() + "\"")
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(data);
  }

}
