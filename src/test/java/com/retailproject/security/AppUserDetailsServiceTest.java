package com.retailproject.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;

import com.retailproject.config.AppSecurityProperties;

class AppUserDetailsServiceTest {
    private LoginAttemptService loginAttemptService;
    private AppUserDetailsService appUserDetailsService;

    @BeforeEach
    void setUp() {
        loginAttemptService = new LoginAttemptService();
        appUserDetailsService = new AppUserDetailsService(
                new AppSecurityProperties(
                        "admin",
                        "admin-123",
                        "analyst",
                        "user-123",
                        List.of()),
                loginAttemptService);
    }

    @Test
    void adminUserLoadsWithAdminAndUserRoles() {
        UserDetails userDetails = appUserDetailsService.loadUserByUsername("admin");

        assertEquals("admin", userDetails.getUsername());
        assertEquals("{noop}admin-123", userDetails.getPassword());
        assertIterableEquals(
                List.of("ROLE_ADMIN", "ROLE_USER"),
                userDetails.getAuthorities().stream().map(authority -> authority.getAuthority()).sorted().toList());
    }

    @Test
    void analystUserLoadsWithUserRoleOnly() {
        UserDetails userDetails = appUserDetailsService.loadUserByUsername("analyst");

        assertEquals("analyst", userDetails.getUsername());
        assertEquals("{noop}user-123", userDetails.getPassword());
        assertIterableEquals(
                List.of("ROLE_USER"),
                userDetails.getAuthorities().stream().map(authority -> authority.getAuthority()).toList());
    }

    @Test
    void encodedPasswordsAreNotModified() {
        AppUserDetailsService encodedPasswordService = new AppUserDetailsService(
                new AppSecurityProperties(
                        "admin",
                        "{bcrypt}encoded-admin",
                        "analyst",
                        "{noop}user-123",
                        List.of()),
                loginAttemptService);

        UserDetails admin = encodedPasswordService.loadUserByUsername("admin");
        UserDetails analyst = encodedPasswordService.loadUserByUsername("analyst");

        assertEquals("{bcrypt}encoded-admin", admin.getPassword());
        assertEquals("{noop}user-123", analyst.getPassword());
    }

    @Test
    void lockedUsersCannotBeLoaded() {
        loginAttemptService.recordFailure("analyst");
        loginAttemptService.recordFailure("analyst");
        loginAttemptService.recordFailure("analyst");

        assertThrows(LockedException.class, () -> appUserDetailsService.loadUserByUsername("analyst"));
    }

    @Test
    void unknownUsersAreRejected() {
        assertThrows(UsernameNotFoundException.class, () -> appUserDetailsService.loadUserByUsername("missing-user"));
    }
}
