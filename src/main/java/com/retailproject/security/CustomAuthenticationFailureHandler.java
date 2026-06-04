package com.retailproject.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private final LoginAttemptService loginAttemptService;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");

        if (exception instanceof LockedException || loginAttemptService.isLocked(username)) {
            getRedirectStrategy().sendRedirect(request, response, "/login?error=locked");
            return;
        }

        boolean lockedNow = loginAttemptService.recordFailure(username);
        if (lockedNow) {
            getRedirectStrategy().sendRedirect(request, response, "/login?error=locked");
            return;
        }

        getRedirectStrategy().sendRedirect(request, response, "/login?error=invalid");
    }
}
