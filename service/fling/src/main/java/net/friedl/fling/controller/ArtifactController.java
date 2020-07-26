package net.friedl.fling.controller;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.friedl.fling.model.dto.ArtifactDto;
import net.friedl.fling.service.ArtifactService;
import net.friedl.fling.service.archive.ArchiveService;

@RestController
@RequestMapping("/api/artifacts")
@Tag(name = "artifact", description = "Operations on /api/artifacts")
@SecurityRequirement(name = "bearer")
@Validated
public class ArtifactController {
  @Value("${fling.max-artifact-size:-1}")
  private Long maxArtifactSize;

  private ArtifactService artifactService;
  private ArchiveService archiveService;

  @Autowired
  public ArtifactController(ArtifactService artifactService, ArchiveService archiveService) {
    this.artifactService = artifactService;
    this.archiveService = archiveService;
  }

  @ApiResponse(responseCode = "404", description = "No artifact with `id` found")
  @GetMapping(path = "/{id}")
  public ArtifactDto getArtifact(@PathVariable UUID id) {
    return artifactService.getById(id);
  }

  @ApiResponse(responseCode = "404", description = "No artifact with `id` found")
  @DeleteMapping(path = "/{id}")
  public void deleteArtifact(@PathVariable UUID id) throws IOException {
    artifactService.delete(id);
  }

  @RequestBody(content = @Content(schema = @Schema(type = "string", format = "binary")))
  @PostMapping(path = "/{id}/data")
  public void uploadArtifactData(@PathVariable UUID id, HttpServletRequest request)
      throws IOException {
    if(maxArtifactSize >= 0 && maxArtifactSize < request.getContentLengthLong()) {
      throw new IOException("Maximum artifact size exceeded");
    }

    archiveService.storeArtifact(id, request.getInputStream());
  }

  @ApiResponse(responseCode = "200",
      content = @Content(schema = @Schema(type = "string", format = "binary")))
  @GetMapping(path = "/{id}/data")
  public ResponseEntity<Resource> downloadArtifact(@PathVariable UUID id) throws IOException {
    ArtifactDto artifactDto = artifactService.getById(id);
    InputStreamResource data = new InputStreamResource(archiveService.getArtifact(id));

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment;filename=\"" + artifactDto.getPath().getFileName() + "\"")
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(data);
  }

}
