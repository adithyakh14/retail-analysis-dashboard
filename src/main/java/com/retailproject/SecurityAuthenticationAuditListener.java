package com.retailproject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class SecurityAuthenticationAuditListener {
    private static final Logger logger = LoggerFactory.getLogger(SecurityAuthenticationAuditListener.class);
    private final LoginAttemptService loginAttemptService;

    public SecurityAuthenticationAuditListener(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        loginAttemptService.reset(event.getAuthentication().getName());
        logger.info("Authentication success for principal='{}'", event.getAuthentication().getName());
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        logger.warn("Authentication failure for principal='{}': {}",
                principal,
                event.getException().getMessage());
    }
}
