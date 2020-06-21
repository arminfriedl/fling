package net.friedl.fling.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import net.friedl.fling.model.dto.ArtifactDto;
import net.friedl.fling.persistence.archive.ArchiveException;
import net.friedl.fling.service.ArtifactService;

@RestController
@RequestMapping("/api")
public class ArtifactController {

  private ArtifactService artifactService;

  @Autowired
  public ArtifactController(ArtifactService artifactService) {
    this.artifactService = artifactService;
  }

  @GetMapping(path = "/artifacts", params = "flingId")
  public List<ArtifactDto> getArtifacts(@RequestParam Long flingId) {
    return artifactService.findAllArtifacts(flingId);
  }

  @GetMapping(path = "/artifacts", params = "artifactId")
  public ResponseEntity<ArtifactDto> getArtifact(@RequestParam Long artifactId) {
    return ResponseEntity.of(artifactService.findArtifact(artifactId));
  }

  @PostMapping("/artifacts/{flingId}")
  public ArtifactDto postArtifact(@PathVariable Long flingId, HttpServletRequest request)
      throws Exception {
    return artifactService.storeArtifact(flingId, request.getInputStream());
  }

  @PatchMapping(path = "/artifacts/{artifactId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ArtifactDto patchArtifact(@PathVariable Long artifactId, @RequestBody String body) {
    return artifactService.mergeArtifact(artifactId, body);
  }

  @DeleteMapping(path = "/artifacts/{artifactId}")
  public void deleteArtifact(@PathVariable Long artifactId) throws ArchiveException {
    artifactService.deleteArtifact(artifactId);
  }

  @GetMapping(path = "/artifacts/{artifactId}/downloadid")
  public String getDownloadId(@PathVariable Long artifactId) {
    return artifactService.generateDownloadId(artifactId);
  }

  @GetMapping(path = "/artifacts/{artifactId}/{downloadId}/download")
  public ResponseEntity<Resource> downloadArtifact(@PathVariable Long artifactId,
      @PathVariable String downloadId)
      throws ArchiveException {

    var artifact = artifactService.findArtifact(artifactId).orElseThrow();
    var stream = new InputStreamResource(artifactService.downloadArtifact(downloadId));

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment;filename=\"" + artifact.getName() + "\"")
        .contentLength(artifact.getSize())
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(stream);
  }

}
