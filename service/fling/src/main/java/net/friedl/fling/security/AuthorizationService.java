package net.friedl.fling.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.friedl.fling.service.FlingService;

@Service
public class AuthorizationService {
    private FlingService flingService;

    @Autowired
    public AuthorizationService(FlingService flingService) {
        this.flingService = flingService;
    }

    public boolean allowUpload(Long flingId) {
        return flingService
                .findFlingById(flingId)
                .orElseThrow()
                .getAllowUpload();
    }
}
