package com.retailproject.web;

import com.retailproject.dto.ApiLoginRequest;
import com.retailproject.security.ApiTokenService;
import com.retailproject.security.LoginAttemptService;
import jakarta.validation.Valid;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthenticationController {
    private static final DateTimeFormatter EXPIRES_AT_FORMAT = DateTimeFormatter.ISO_INSTANT;

    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService loginAttemptService;
    private final ApiTokenService apiTokenService;

    public ApiAuthenticationController(
            AuthenticationManager authenticationManager,
            LoginAttemptService loginAttemptService,
            ApiTokenService apiTokenService) {
        this.authenticationManager = authenticationManager;
        this.loginAttemptService = loginAttemptService;
        this.apiTokenService = apiTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody ApiLoginRequest request) {
        String username = request.username();
        String password = request.password();

        if (loginAttemptService.isLocked(username)) {
            return ResponseEntity.status(HttpStatus.LOCKED).body(errorBody(
                    "locked",
                    "Too many failed attempts. This account is temporarily locked for 5 minutes."));
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            loginAttemptService.reset(username);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            ApiTokenService.TokenSession session = apiTokenService.issueToken(userDetails);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("accessToken", session.token());
            response.put("tokenType", "Bearer");
            response.put("username", session.username());
            response.put("roles", session.authorities());
            response.put("expiresAt", EXPIRES_AT_FORMAT.format(session.expiresAt()));
            return ResponseEntity.ok(response);
        } catch (LockedException exception) {
            return ResponseEntity.status(HttpStatus.LOCKED).body(errorBody(
                    "locked",
                    "Too many failed attempts. This account is temporarily locked for 5 minutes."));
        } catch (AuthenticationException exception) {
            boolean lockedNow = loginAttemptService.recordFailure(username);
            HttpStatus status = lockedNow ? HttpStatus.LOCKED : HttpStatus.UNAUTHORIZED;
            String code = lockedNow ? "locked" : "invalid";
            String message = lockedNow
                    ? "Too many failed attempts. This account is temporarily locked for 5 minutes."
                    : "Invalid username or password.";
            return ResponseEntity.status(status).body(errorBody(code, message));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            apiTokenService.revokeToken(authorizationHeader.substring(7).trim());
        }

        return ResponseEntity.ok(Map.of("message", "Signed out."));
    }

    private Map<String, Object> errorBody(String code, String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", code);
        response.put("message", message);
        return response;
    }
}
