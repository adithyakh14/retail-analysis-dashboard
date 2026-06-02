package com.retailproject;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final LoginAttemptService loginAttemptService;
    private final Map<String, UserDetails> usersByUsername;

    public AppUserDetailsService(
            AppSecurityProperties properties,
            LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
        this.usersByUsername = new HashMap<>();
        usersByUsername.put(properties.username(), User.withUsername(properties.username())
                .password(normalizePassword(properties.password()))
                .roles("USER", "ADMIN")
                .build());
        usersByUsername.put(properties.userUsername(), User.withUsername(properties.userUsername())
                .password(normalizePassword(properties.userPassword()))
                .roles("USER")
                .build());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (loginAttemptService.isLocked(username)) {
            throw new LockedException("Account temporarily locked after repeated failed logins.");
        }

        UserDetails user = usersByUsername.get(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return user;
    }

    private String normalizePassword(String configuredPassword) {
        if (configuredPassword == null || configuredPassword.isBlank()) {
            throw new IllegalArgumentException("Configured security password must not be blank.");
        }
        return configuredPassword.startsWith("{") ? configuredPassword : "{noop}" + configuredPassword;
    }
}
