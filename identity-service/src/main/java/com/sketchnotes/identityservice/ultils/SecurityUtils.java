package com.sketchnotes.identityservice.ultils;

import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.repository.IUserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@RequiredArgsConstructor
public class SecurityUtils {
    private final IUserRepository userRepository;
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return jwt.getSubject();
        }
        throw  new AppException(ErrorCode.UNAUTHENTICATED);
    }
    
    /**
     * Get current user's keycloak ID and convert to database user ID
     * This method retrieves the keycloak ID from JWT and looks up the corresponding user ID
     */
    public static Long getCurrentUserIdAsLong() {
        String keycloakId = getCurrentUserId();
        // Note: This requires injecting IUserRepository in the calling service
        // For now, we'll throw an exception to indicate this needs to be handled in the service layer
        throw new UnsupportedOperationException("Use service layer to convert keycloak ID to user ID");
    }

}
