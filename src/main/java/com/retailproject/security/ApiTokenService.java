package com.retailproject.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class ApiTokenService {
    private final Map<String, TokenSession> sessionsByToken = new ConcurrentHashMap<>();
    private final Duration tokenLifetime = Duration.ofHours(8);

    public TokenSession issueToken(UserDetails userDetails) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(tokenLifetime);
        String token = UUID.randomUUID().toString();

        TokenSession session = new TokenSession(
                token,
                userDetails.getUsername(),
                userDetails.getAuthorities().stream()
                        .map((@NonNull GrantedAuthority authority) -> authority.getAuthority())
                        .toList(),
                expiresAt);

        sessionsByToken.put(token, session);
        return session;
    }

    public TokenSession findValidSession(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        TokenSession session = sessionsByToken.get(token.trim());
        if (session == null) {
            return null;
        }

        if (session.expiresAt().isAfter(Instant.now())) {
            return session;
        }

        sessionsByToken.remove(token.trim());
        return null;
    }

    public void revokeToken(String token) {
        if (token != null && !token.isBlank()) {
            sessionsByToken.remove(token.trim());
        }
    }

    public record TokenSession(String token, String username, Collection<String> authorities, Instant expiresAt) {
    }
}
