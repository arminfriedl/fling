package net.friedl.fling.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.friedl.fling.security.authentication.FlingToken;
import net.friedl.fling.security.authentication.dto.UserAuthDto;
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

    public boolean allowFlingAccess(UserAuthDto userAuth, String shareUrl) {
        return userAuth.getShareId().equals(shareUrl);
    }

    public boolean allowFlingAccess(Long flingId) {
        return false;
    }

    public boolean allowFlingAccess(FlingToken authentication, HttpServletRequest request) {
        if(authentication.getGrantedFlingAuthority().getAuthority().equals(FlingAuthority.FLING_OWNER.name())) {
            return true;
        }

        var shareId = request.getParameter("shareId");
        var flingId = shareId != null
             ? flingService.findFlingByShareId(shareId).orElseThrow().getId()
             : request.getParameter("flingId");

        return authentication.getGrantedFlingAuthority().getFlingId().equals(flingId);
    }
}
