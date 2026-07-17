package com.retailproject.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.retailproject.dto.ApiLoginRequest;
import com.retailproject.security.ApiTokenService;
import com.retailproject.security.LoginAttemptService;

class ApiAuthenticationControllerTest {
    private AuthenticationManager authenticationManager;
    private LoginAttemptService loginAttemptService;
    private ApiTokenService apiTokenService;
    private ApiAuthenticationController controller;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        loginAttemptService = mock(LoginAttemptService.class);
        apiTokenService = mock(ApiTokenService.class);
        controller = new ApiAuthenticationController(authenticationManager, loginAttemptService, apiTokenService);
    }

    @Test
    void lockedUserGetsLockedResponseBeforeAuthentication() {
        when(loginAttemptService.isLocked("analyst")).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.login(new ApiLoginRequest("analyst", "user-123"));
        Map<String, Object> body = response.getBody();

        assertEquals(HttpStatus.LOCKED, response.getStatusCode());
        assertNotNull(body);
        assertEquals("locked", body.get("error"));
    }

    @Test
    void successfulLoginReturnsTokenPayloadAndResetsAttempts() {
        UserDetails userDetails = new User("admin", "ignored", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(loginAttemptService.isLocked("admin")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(apiTokenService.issueToken(userDetails)).thenReturn(
                new ApiTokenService.TokenSession("token-123", "admin", List.of("ROLE_ADMIN"), Instant.now().plusSeconds(60)));

        ResponseEntity<Map<String, Object>> response = controller.login(new ApiLoginRequest("admin", "admin-123"));
        Map<String, Object> body = response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(body);
        assertEquals("token-123", body.get("accessToken"));
        assertEquals("admin", body.get("username"));
        verify(loginAttemptService).reset("admin");
    }

    @Test
    void invalidCredentialsReturnUnauthorizedUntilLockThreshold() {
        when(loginAttemptService.isLocked("analyst")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        when(loginAttemptService.recordFailure("analyst")).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = controller.login(new ApiLoginRequest("analyst", "bad-password"));
        Map<String, Object> body = response.getBody();

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(body);
        assertEquals("invalid", body.get("error"));
        assertEquals("Invalid username or password.", body.get("message"));
    }

    @Test
    void invalidCredentialsCanEscalateToLockedResponse() {
        when(loginAttemptService.isLocked("analyst")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        when(loginAttemptService.recordFailure("analyst")).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.login(new ApiLoginRequest("analyst", "bad-password"));
        Map<String, Object> body = response.getBody();

        assertEquals(HttpStatus.LOCKED, response.getStatusCode());
        assertNotNull(body);
        assertEquals("locked", body.get("error"));
    }

    @Test
    void lockedExceptionFromAuthenticationManagerReturnsLockedResponse() {
        when(loginAttemptService.isLocked("analyst")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new LockedException("Locked"));

        ResponseEntity<Map<String, Object>> response = controller.login(new ApiLoginRequest("analyst", "user-123"));
        Map<String, Object> body = response.getBody();

        assertEquals(HttpStatus.LOCKED, response.getStatusCode());
        assertNotNull(body);
        assertEquals("locked", body.get("error"));
    }

    @Test
    void logoutRevokesBearerTokenAndReturnsSignedOutMessage() {
        ResponseEntity<Map<String, Object>> response = controller.logout("Bearer token-123");
        Map<String, Object> body = response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(body);
        assertEquals("Signed out.", body.get("message"));
        verify(apiTokenService).revokeToken("token-123");
    }

    @Test
    void logoutWithoutAuthorizationHeaderStillSucceeds() {
        ResponseEntity<Map<String, Object>> response = controller.logout(null);
        Map<String, Object> body = response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(body);
        assertEquals("Signed out.", body.get("message"));
    }
}
