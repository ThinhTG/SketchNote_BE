package com.sketchnotes.payment_service.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserPrincipalJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Map<String, Object> claims = jwt.getClaims();

        // Adapt to your Identity claims
        String username = (String) claims.getOrDefault("preferred_username", claims.getOrDefault("sub", "anonymous"));

        // Try common places for user id: custom claim "userId", or numeric sub, or "uid"
        Long userId = null;
        Object uidClaim = claims.get("userId");
        if (uidClaim == null) uidClaim = claims.get("uid");
        if (uidClaim == null) {
            Object sub = claims.get("sub");
            if (sub instanceof String s && s.matches("\\d+")) {
                uidClaim = Long.parseLong(s);
            }
        }
        if (uidClaim instanceof Number n) {
            userId = n.longValue();
        } else if (uidClaim instanceof String s && s.matches("\\d+")) {
            userId = Long.parseLong(s);
        }

        UserPrincipal principal = new UserPrincipal(userId, username);

        return new JwtAuthenticationToken(jwt, principal.getAuthorities(), principal.getUsername()) {
            @Override
            public Object getPrincipal() {
                return principal;
            }
        };
    }
}


