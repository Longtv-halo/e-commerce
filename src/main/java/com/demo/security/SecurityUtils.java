package com.demo.security;

import org.springframework.security.oauth2.jwt.Jwt;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@NoArgsConstructor
@Component
public class SecurityUtils {

    private static Jwt getJwt() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        System.out.println(authentication.getClass());
        System.out.println(authentication.getPrincipal());
        System.out.println(authentication.getPrincipal().getClass());
        System.out.println(authentication.getAuthorities());
        if (!(authentication instanceof JwtAuthenticationToken token)) {
            throw new IllegalStateException(
                    "Current authentication is not JwtAuthenticationToken. Actual type: "
                            + authentication.getClass().getName()
            );
        }

        return token.getToken();
    }

    public static String currentUserId() {
        return getJwt().getSubject();
    }

    public static String currentUsername() {
        return getJwt().getClaimAsString("preferred_username");
    }
}