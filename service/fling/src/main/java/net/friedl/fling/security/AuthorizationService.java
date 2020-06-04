package net.friedl.fling.security;

import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.friedl.fling.security.authentication.FlingToken;
import net.friedl.fling.security.authentication.dto.UserAuthDto;
import net.friedl.fling.service.FlingService;

@Slf4j
@Service
public class AuthorizationService {
    private FlingService flingService;

    @Autowired
    public AuthorizationService(FlingService flingService) {
        this.flingService = flingService;
    }

    public boolean allowUpload(Long flingId, FlingToken authentication) {
        if (authentication.getGrantedFlingAuthority().getAuthority().equals(FlingAuthority.FLING_OWNER.name())) {
            return true;
        }

        return flingService.findFlingById(flingId).orElseThrow().getAllowUpload()
                && authentication.getGrantedFlingAuthority().getFlingId().equals(flingId);
    }

    public boolean allowFlingAccess(UserAuthDto userAuth, String shareUrl) {
        return userAuth.getShareId().equals(shareUrl);
    }

    public boolean allowFlingAccess(Long flingId, FlingToken authentication) {
        if (authentication.getGrantedFlingAuthority().getAuthority().equals(FlingAuthority.FLING_OWNER.name())) {
            return true;
        }

        return authentication.getGrantedFlingAuthority().getFlingId().equals(flingId);
    }

    public boolean allowFlingAccess(FlingToken authentication, HttpServletRequest request) {
        if (authentication.getGrantedFlingAuthority().getAuthority().equals(FlingAuthority.FLING_OWNER.name())) {
            return true;
        }

        var shareId = request.getParameter("shareId");

        Long flingId;

        try {
            flingId = shareId != null
                    ? flingService.findFlingByShareId(shareId).orElseThrow().getId()
                    : Long.parseLong(request.getParameter("flingId"));
        } catch (NumberFormatException | NoSuchElementException e) {
            log.warn("Invalid shareId [shareId=\"{}\"] or flingId [flingId=\"{}\"] found", request.getParameter("shareId"), request.getParameter("flingId"));
            flingId = null;
        }

        return authentication.getGrantedFlingAuthority().getFlingId().equals(flingId);
    }
}
