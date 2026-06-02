package com.retailproject;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SecurityAuditFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(SecurityAuditFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !(path.startsWith("/download/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        filterChain.doFilter(request, response);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principal = authentication != null ? authentication.getName() : "anonymous";

        logger.info("Security audit: principal='{}' method={} path='{}' status={}",
                principal,
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus());
    }
}
