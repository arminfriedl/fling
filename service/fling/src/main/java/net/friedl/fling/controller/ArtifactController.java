package net.friedl.fling.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.friedl.fling.model.dto.ArtifactDto;
import net.friedl.fling.service.ArtifactService;

@RestController
@RequestMapping("/api")
public class ArtifactController {

    private ArtifactService artifactService;

    @Autowired
    public ArtifactController(ArtifactService artifactService) {
        this.artifactService = artifactService;
    }

    @GetMapping(path = "/artifacts", params="flingId")
    public List<ArtifactDto> getArtifacts(@RequestParam Long flingId) {
        return artifactService.findAllArtifacts(flingId);
    }

    @GetMapping(path = "/artifacts", params="artifactId")
    public ResponseEntity<ArtifactDto> getArtifact(@RequestParam Long artifactId) {
        return ResponseEntity.of(artifactService.findArtifact(artifactId));
    }

    @PostMapping("/artifacts/{flingId}")
    public ArtifactDto postArtifact(@PathVariable Long flingId, HttpServletRequest request) throws Exception {
        return artifactService.storeArtifact(flingId, request.getInputStream());
    }

    @PatchMapping(path = "/artifacts/{artifactId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ArtifactDto patchArtifactDto(@PathVariable Long artifactId, @RequestBody String body) {
        return artifactService.mergeArtifact(artifactId, body);
    }
}
