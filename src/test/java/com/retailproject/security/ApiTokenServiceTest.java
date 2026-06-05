package com.retailproject.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class ApiTokenServiceTest {
    private ApiTokenService apiTokenService;

    @BeforeEach
    void setUp() {
        apiTokenService = new ApiTokenService();
    }

    @Test
    void issueTokenCreatesSessionWithUsernameRolesAndExpiry() {
        UserDetails userDetails = user("admin", "ROLE_ADMIN", "ROLE_USER");

        ApiTokenService.TokenSession session = apiTokenService.issueToken(userDetails);

        assertNotNull(session.token());
        assertEquals("admin", session.username());
        assertEquals(List.of("ROLE_ADMIN", "ROLE_USER"), List.copyOf(session.authorities()));
        assertTrue(session.expiresAt().isAfter(java.time.Instant.now()));
    }

    @Test
    void issuedTokenCanBeResolvedToValidSession() {
        ApiTokenService.TokenSession issuedSession = apiTokenService.issueToken(user("analyst", "ROLE_USER"));

        ApiTokenService.TokenSession resolvedSession = apiTokenService.findValidSession(issuedSession.token());

        assertNotNull(resolvedSession);
        assertEquals(issuedSession.token(), resolvedSession.token());
        assertEquals("analyst", resolvedSession.username());
    }

    @Test
    void revokedTokenIsNoLongerValid() {
        ApiTokenService.TokenSession issuedSession = apiTokenService.issueToken(user("analyst", "ROLE_USER"));

        apiTokenService.revokeToken(issuedSession.token());

        assertNull(apiTokenService.findValidSession(issuedSession.token()));
    }

    @Test
    void blankOrNullTokensReturnNoSession() {
        assertNull(apiTokenService.findValidSession(null));
        assertNull(apiTokenService.findValidSession(""));
        assertNull(apiTokenService.findValidSession("   "));
    }

    @Test
    void revokeTokenIgnoresBlankValuesSafely() {
        apiTokenService.revokeToken(null);
        apiTokenService.revokeToken("");
        apiTokenService.revokeToken("   ");
    }

    private UserDetails user(String username, String... roles) {
        return new User(
                username,
                "ignored-password",
                java.util.Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList());
    }
}
