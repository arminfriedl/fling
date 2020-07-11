package net.friedl.fling.controller;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.friedl.fling.model.dto.ArtifactDto;
import net.friedl.fling.model.dto.FlingDto;
import net.friedl.fling.service.ArtifactService;
import net.friedl.fling.service.FlingService;
import net.friedl.fling.service.archive.ArchiveService;

@RestController
@RequestMapping("/api/fling")
@Tag(name = "fling", description = "Operations on /api/fling")
public class FlingController {

  private FlingService flingService;
  private ArtifactService artifactService;
  private ArchiveService archiveService;

  @Autowired
  public FlingController(FlingService flingService, ArtifactService artifactService,
      ArchiveService archiveService) {

    this.flingService = flingService;
    this.artifactService = artifactService;
    this.archiveService = archiveService;
  }

  @GetMapping
  public List<FlingDto> getFlings() {
    return flingService.findAll();
  }

  @PostMapping
  public FlingDto postFling(@RequestBody @Valid FlingDto flingDto) {
    return flingService.create(flingDto);
  }

  @PostMapping("/{id}/artifacts")
  public ArtifactDto postArtifact(@PathVariable UUID id,
      @RequestBody @Valid ArtifactDto artifactDto) {
    return artifactService.create(id, artifactDto);
  }

  @GetMapping("/{id}/artifacts")
  public Set<ArtifactDto> getArtifacts(@PathVariable UUID id) {
    return flingService.getArtifacts(id);
  }

  @GetMapping(path = "/{id}")
  public FlingDto getFling(@PathVariable UUID id) {
    return flingService.getById(id);
  }

  @GetMapping(path = "/share/{shareId}")
  public FlingDto getFlingByShareId(@PathVariable String shareId) {
    return flingService.getByShareId(shareId);
  }

  @DeleteMapping("/{id}")
  public void deleteFling(@PathVariable UUID id) throws IOException {
    flingService.delete(id);
  }

  @Operation(responses = {
      @ApiResponse(responseCode = "200",
          content = @Content(
              mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
              schema = @Schema(type = "string", format = "binary")))
  })
  @GetMapping(path = "/{id}/data")
  public ResponseEntity<Resource> getFlingData(@PathVariable UUID id) throws IOException {
    FlingDto flingDto = flingService.getById(id);
    InputStreamResource data = new InputStreamResource(archiveService.getFling(id));

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment;filename=\"" + flingDto.getName() + ".zip" + "\"")
        .contentLength(200L) // FIXME
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(data);
  }

}
