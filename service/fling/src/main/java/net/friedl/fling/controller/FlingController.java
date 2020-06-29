package net.friedl.fling.controller;

import java.util.List;
import java.util.UUID;
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
import net.friedl.fling.model.dto.ArtifactDto;
import net.friedl.fling.model.dto.FlingDto;
import net.friedl.fling.service.ArtifactService;
import net.friedl.fling.service.FlingService;
import net.friedl.fling.service.archive.ArchiveService;

@RestController
@RequestMapping("/api/fling")
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
  public FlingDto postFling(@RequestBody FlingDto flingDto) {
    return flingService.create(flingDto);
  }

  @PostMapping("/{id}/artifact")
  public ArtifactDto postArtifact(@PathVariable UUID id, @RequestBody ArtifactDto artifactDto) {
    return artifactService.create(id, artifactDto);
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
  public void deleteFling(@PathVariable UUID id) {
    flingService.delete(id);
  }

  @GetMapping(path = "/{id}/data")
  public ResponseEntity<Resource> getFlingData(@PathVariable UUID id) {
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
