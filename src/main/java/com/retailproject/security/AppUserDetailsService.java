package com.retailproject.security;

import com.retailproject.config.AppSecurityProperties;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final LoginAttemptService loginAttemptService;
    private final Map<String, AccountProfile> usersByUsername;

    public AppUserDetailsService(
            AppSecurityProperties properties,
            LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
        this.usersByUsername = new HashMap<>();
        usersByUsername.put(properties.username(), new AccountProfile(
                properties.username(),
                normalizePassword(properties.password()),
                Set.of("USER", "ADMIN")));
        usersByUsername.put(properties.userUsername(), new AccountProfile(
                properties.userUsername(),
                normalizePassword(properties.userPassword()),
                Set.of("USER")));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (loginAttemptService.isLocked(username)) {
            throw new LockedException("Account temporarily locked after repeated failed logins.");
        }

        AccountProfile profile = usersByUsername.get(username);
        if (profile == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return User.withUsername(profile.username())
                .password(profile.password())
                .roles(profile.roles().toArray(String[]::new))
                .build();
    }

    private String normalizePassword(String configuredPassword) {
        if (configuredPassword == null || configuredPassword.isBlank()) {
            throw new IllegalArgumentException("Configured security password must not be blank.");
        }
        return configuredPassword.startsWith("{") ? configuredPassword : "{noop}" + configuredPassword;
    }

    private record AccountProfile(String username, String password, Set<String> roles) {
    }
}
