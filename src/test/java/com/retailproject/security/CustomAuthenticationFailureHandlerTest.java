package com.retailproject.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;

class CustomAuthenticationFailureHandlerTest {
    private LoginAttemptService loginAttemptService;
    private CustomAuthenticationFailureHandler failureHandler;

    @BeforeEach
    void setUp() {
        loginAttemptService = new LoginAttemptService();
        failureHandler = new CustomAuthenticationFailureHandler(loginAttemptService);
    }

    @Test
    void lockedExceptionRedirectsToLockedMessage() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("username", "analyst");
        MockHttpServletResponse response = new MockHttpServletResponse();

        failureHandler.onAuthenticationFailure(
                request,
                response,
                new LockedException("Account is locked"));

        assertEquals("/login?error=locked", response.getRedirectedUrl());
    }

    @Test
    void invalidCredentialsRedirectToInvalidMessageBeforeLockThreshold() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("username", "analyst");
        MockHttpServletResponse response = new MockHttpServletResponse();

        failureHandler.onAuthenticationFailure(
                request,
                response,
                new BadCredentialsException("Bad credentials"));

        assertEquals("/login?error=invalid", response.getRedirectedUrl());
    }

    @Test
    void thirdFailureRedirectsToLockedMessage() throws Exception {
        loginAttemptService.recordFailure("analyst");
        loginAttemptService.recordFailure("analyst");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("username", "analyst");
        MockHttpServletResponse response = new MockHttpServletResponse();

        failureHandler.onAuthenticationFailure(
                request,
                response,
                new BadCredentialsException("Bad credentials"));

        assertEquals("/login?error=locked", response.getRedirectedUrl());
    }

    @Test
    void alreadyLockedUserRedirectsToLockedMessageWithoutRecordingAnotherFailure() throws Exception {
        loginAttemptService.recordFailure("analyst");
        loginAttemptService.recordFailure("analyst");
        loginAttemptService.recordFailure("analyst");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("username", "analyst");
        MockHttpServletResponse response = new MockHttpServletResponse();

        failureHandler.onAuthenticationFailure(
                request,
                response,
                new BadCredentialsException("Bad credentials"));

        assertEquals("/login?error=locked", response.getRedirectedUrl());
    }
}
