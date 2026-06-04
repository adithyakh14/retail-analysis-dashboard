package com.retailproject.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityAuthenticationAuditListener {
    private final LoginAttemptService loginAttemptService;

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        loginAttemptService.reset(event.getAuthentication().getName());
        log.info("Authentication success for principal='{}'", event.getAuthentication().getName());
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        log.warn("Authentication failure for principal='{}': {}",
                principal,
                event.getException().getMessage());
    }
}
