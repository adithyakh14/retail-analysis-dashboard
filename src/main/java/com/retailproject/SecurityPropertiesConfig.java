package com.retailproject;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AppSecurityProperties.class)
public class SecurityPropertiesConfig {
}
