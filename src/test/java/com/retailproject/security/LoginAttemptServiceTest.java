package com.retailproject.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginAttemptServiceTest {
    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        loginAttemptService = new LoginAttemptService();
    }

    @Test
    void firstTwoFailuresDoNotLockUser() {
        assertFalse(loginAttemptService.recordFailure("analyst"));
        assertFalse(loginAttemptService.recordFailure("analyst"));
        assertFalse(loginAttemptService.isLocked("analyst"));
    }

    @Test
    void thirdFailureLocksUser() {
        loginAttemptService.recordFailure("analyst");
        loginAttemptService.recordFailure("analyst");

        assertTrue(loginAttemptService.recordFailure("analyst"));
        assertTrue(loginAttemptService.isLocked("analyst"));
    }

    @Test
    void resetClearsFailureStateAndLock() {
        loginAttemptService.recordFailure("analyst");
        loginAttemptService.recordFailure("analyst");
        loginAttemptService.recordFailure("analyst");

        loginAttemptService.reset("analyst");

        assertFalse(loginAttemptService.isLocked("analyst"));
        assertFalse(loginAttemptService.recordFailure("analyst"));
    }

    @Test
    void usernamesAreNormalizedBeforeTrackingFailures() {
        loginAttemptService.recordFailure(" Analyst ");
        loginAttemptService.recordFailure("ANALYST");

        assertTrue(loginAttemptService.recordFailure("analyst"));
        assertTrue(loginAttemptService.isLocked(" analyst "));
    }

    @Test
    void blankOrNullUsernamesDoNotTriggerLocking() {
        assertFalse(loginAttemptService.recordFailure(null));
        assertFalse(loginAttemptService.recordFailure("   "));
        assertFalse(loginAttemptService.isLocked(null));
        assertFalse(loginAttemptService.isLocked("   "));
    }
}
