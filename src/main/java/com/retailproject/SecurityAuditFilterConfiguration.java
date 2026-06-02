package com.retailproject;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityAuditFilterConfiguration {
    @Bean
    FilterRegistrationBean<SecurityAuditFilter> securityAuditFilterRegistration(SecurityAuditFilter securityAuditFilter) {
        FilterRegistrationBean<SecurityAuditFilter> registration = new FilterRegistrationBean<>(securityAuditFilter);
        registration.setEnabled(false);
        return registration;
    }
}
