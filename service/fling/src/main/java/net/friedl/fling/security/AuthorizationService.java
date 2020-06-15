package net.friedl.fling.security;

import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.friedl.fling.security.authentication.FlingToken;
import net.friedl.fling.security.authentication.dto.UserAuthDto;
import net.friedl.fling.service.ArtifactService;
import net.friedl.fling.service.FlingService;

@Slf4j
@Service
public class AuthorizationService {
    private FlingService flingService;
    private ArtifactService artifactService;

    @Autowired
    public AuthorizationService(FlingService flingService, ArtifactService artifactService) {
        this.flingService = flingService;
        this.artifactService = artifactService;
    }

    public boolean allowUpload(Long flingId, AbstractAuthenticationToken token) {
    	if (!(token instanceof FlingToken)) return false;

    	FlingToken flingToken = (FlingToken) token;
        if (flingToken.getGrantedFlingAuthority().getAuthority().equals(FlingAuthority.FLING_OWNER.name())) {
            return true;
        }

        var uploadAllowed = flingService.findFlingById(flingId).orElseThrow().getAllowUpload();

        return uploadAllowed && flingToken.getGrantedFlingAuthority().getFlingId().equals(flingId);
    }

    public boolean allowPatchingArtifact(Long artifactId, FlingToken authentication) {
        var flingId = artifactService.findArtifact(artifactId).orElseThrow().getFling().getId();
        return allowUpload(flingId, authentication);
    }

    public boolean allowFlingAccess(UserAuthDto userAuth, String shareUrl) {
        return userAuth.getShareId().equals(shareUrl);
    }

    public boolean allowFlingAccess(Long flingId, AbstractAuthenticationToken token) {
    	if (!(token instanceof FlingToken)) return false;
    	
    	FlingToken flingToken = (FlingToken) token;
        if (flingToken.getGrantedFlingAuthority().getAuthority().equals(FlingAuthority.FLING_OWNER.name())) {
            return true;
        }

        return flingToken.getGrantedFlingAuthority().getFlingId().equals(flingId);
    }

    public boolean allowFlingAccess(AbstractAuthenticationToken token, HttpServletRequest request) {
    	if (!(token instanceof FlingToken)) return false;

    	FlingToken flingToken = (FlingToken) token;
        if (flingToken.getGrantedFlingAuthority().getAuthority().equals(FlingAuthority.FLING_OWNER.name())) {
            return true;
        }

        var shareId = request.getParameter("shareId");

        Long flingId;

        try {
            flingId = shareId != null
                    ? flingService.findFlingByShareId(shareId).orElseThrow().getId()
                    : Long.parseLong(request.getParameter("flingId"));
        } catch (NumberFormatException | NoSuchElementException e) {
            log.warn("Invalid shareId [shareId=\"{}\"] or flingId [flingId=\"{}\"] found",
                    request.getParameter("shareId"), request.getParameter("flingId"));
            flingId = null;
        }

        return flingToken.getGrantedFlingAuthority().getFlingId().equals(flingId);
    }
}
