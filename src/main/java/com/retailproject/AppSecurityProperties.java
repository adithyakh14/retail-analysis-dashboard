package com.retailproject;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record AppSecurityProperties(
        String username,
        String password,
        String userUsername,
        String userPassword,
        List<String> allowedOrigins) {
}
