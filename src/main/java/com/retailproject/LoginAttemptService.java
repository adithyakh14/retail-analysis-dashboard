package com.retailproject;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {
    private final Map<String, Integer> failureCounts = new ConcurrentHashMap<>();
    private final Map<String, Instant> lockedUntilByUser = new ConcurrentHashMap<>();
    private final int maxAttempts = 3;
    private final Duration lockDuration = Duration.ofMinutes(5);

    public boolean recordFailure(String username) {
        String key = normalize(username);
        if (key.isBlank()) {
            return false;
        }

        Instant now = Instant.now();
        Instant lockedUntil = lockedUntilByUser.get(key);
        if (lockedUntil != null && lockedUntil.isAfter(now)) {
            return true;
        }
        if (lockedUntil != null) {
            lockedUntilByUser.remove(key);
            failureCounts.remove(key);
        }

        int nextFailures = failureCounts.getOrDefault(key, 0) + 1;
        failureCounts.put(key, nextFailures);

        if (nextFailures >= maxAttempts) {
            lockedUntilByUser.put(key, now.plus(lockDuration));
            return true;
        }

        return false;
    }

    public void reset(String username) {
        String key = normalize(username);
        if (!key.isBlank()) {
            failureCounts.remove(key);
            lockedUntilByUser.remove(key);
        }
    }

    public boolean isLocked(String username) {
        String key = normalize(username);
        if (key.isBlank()) {
            return false;
        }

        Instant lockedUntil = lockedUntilByUser.get(key);
        if (lockedUntil == null) {
            return false;
        }

        if (lockedUntil.isAfter(Instant.now())) {
            return true;
        }

        lockedUntilByUser.remove(key);
        failureCounts.remove(key);
        return false;
    }

    private String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}
