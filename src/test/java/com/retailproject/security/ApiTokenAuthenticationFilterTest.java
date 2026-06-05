package com.retailproject.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;

class ApiTokenAuthenticationFilterTest {
    private ApiTokenService apiTokenService;
    private ApiTokenAuthenticationFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        apiTokenService = mock(ApiTokenService.class);
        filter = new ApiTokenAuthenticationFilter(apiTokenService);
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validBearerTokenPopulatesSecurityContextForProtectedApi() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/filter-options");
        request.addHeader("Authorization", "Bearer token-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(apiTokenService.findValidSession("token-123"))
                .thenReturn(new ApiTokenService.TokenSession(
                        "token-123",
                        "analyst",
                        List.of("ROLE_USER"),
                        Instant.now().plusSeconds(60)));

        filter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("analyst", SecurityContextHolder.getContext().getAuthentication().getName());
        assertEquals("token-123", SecurityContextHolder.getContext().getAuthentication().getCredentials());
    }

    @Test
    void invalidBearerTokenLeavesSecurityContextEmpty() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/filter-options");
        request.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(apiTokenService.findValidSession("bad-token")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void authEndpointsAreSkippedByFilter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.addHeader("Authorization", "Bearer token-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void nonProtectedPathsAreSkippedByFilter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login");
        request.addHeader("Authorization", "Bearer token-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
