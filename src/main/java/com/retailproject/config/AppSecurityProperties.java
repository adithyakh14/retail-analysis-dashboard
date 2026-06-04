package com.retailproject.config;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.security")
public record AppSecurityProperties(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String userUsername,
        @NotBlank String userPassword,
        @DefaultValue List<String> allowedOrigins) {
    public AppSecurityProperties {
        allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
    }
}
