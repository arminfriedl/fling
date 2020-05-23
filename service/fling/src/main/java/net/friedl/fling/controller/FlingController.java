package net.friedl.fling.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.friedl.fling.model.dto.FlingDto;
import net.friedl.fling.service.FlingService;

@RestController
@RequestMapping("/api")
public class FlingController {

    private FlingService flingService;

    @Autowired
    public FlingController(FlingService flingService) {
        this.flingService = flingService;
    }

    @GetMapping("/fling")
    public List<FlingDto> getFlings() {
        return flingService.findAll();
    }

    @PostMapping("/fling")
    public Long postFling(@RequestBody FlingDto flingDto) {
        return flingService.createFling(flingDto);
    }

    @PutMapping("/fling/{flingId}")
    public void putFling(@PathVariable Long flingId, @RequestBody FlingDto flingDto) {
        flingService.mergeFling(flingId, flingDto);
    }

    @GetMapping(path = "/fling", params = "flingId")
    public ResponseEntity<FlingDto> getFling(@RequestParam Long flingId) {
        return ResponseEntity.of(flingService.findFlingById(flingId));
    }

    @GetMapping(path = "/fling", params = "shareId")
    public ResponseEntity<FlingDto> getFlingByShareId(@RequestParam String shareId) {
        return ResponseEntity.of(flingService.findFlingByShareId(shareId));
    }

    @GetMapping(path = "/fling/shareExists/{shareId}")
    public Boolean getShareExists(@PathVariable String shareId) {
        return flingService.existsShareUrl(shareId);
    }

    @DeleteMapping("/fling/{flingId}")
    public void deleteFling(@PathVariable Long flingId) {
        flingService.deleteFlingById(flingId);
    }
}
