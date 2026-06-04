package com.retailproject.web;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoginController {
    @GetMapping("/login")
    @ResponseBody
    public ResponseEntity<String> login(
            CsrfToken csrfToken,
            @RequestParam(name = "error", defaultValue = "") String error,
            @RequestParam(name = "logout", defaultValue = "false") boolean logout) {
        boolean locked = "locked".equalsIgnoreCase(error);
        boolean invalid = "invalid".equalsIgnoreCase(error) || "true".equalsIgnoreCase(error);

        String message = logout
                ? "You have been signed out."
                : locked
                ? "Too many failed attempts. This account is temporarily locked for 5 minutes."
                : invalid
                ? "Invalid username or password."
                : "Sign in to open the protected dashboard and APIs.";

        String messageClass = logout ? "notice-success" : (locked || invalid) ? "notice-error" : "notice-info";

        String csrfField = "";
        if (csrfToken != null) {
            csrfField = """
                    <input type="hidden" name="__CSRF_NAME__" value="__CSRF_TOKEN__">
                    """
                    .replace("__CSRF_NAME__", csrfToken.getParameterName())
                    .replace("__CSRF_TOKEN__", csrfToken.getToken());
        }

        String htmlTemplate = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Retail Dashboard Login</title>
                    <style>
                        :root {
                            --bg-top: #f8efe4;
                            --bg-bottom: #ead7c1;
                            --panel: rgba(255, 250, 243, 0.95);
                            --ink: #1f2430;
                            --muted: #5d6470;
                            --accent: #c95c2b;
                            --accent-deep: #8f3d17;
                            --teal: #1f7a72;
                            --line: rgba(31, 36, 48, 0.12);
                        }

                        * { box-sizing: border-box; }

                        body {
                            margin: 0;
                            min-height: 100vh;
                            display: grid;
                            place-items: center;
                            padding: 24px;
                            font-family: "Trebuchet MS", "Segoe UI", sans-serif;
                            color: var(--ink);
                            background:
                                radial-gradient(circle at top left, rgba(201, 92, 43, 0.22), transparent 24%),
                                radial-gradient(circle at bottom right, rgba(31, 122, 114, 0.18), transparent 28%),
                                linear-gradient(180deg, var(--bg-top), var(--bg-bottom));
                        }

                        .login-card {
                            width: min(100%, 460px);
                            background: var(--panel);
                            border: 1px solid rgba(255, 255, 255, 0.7);
                            border-radius: 28px;
                            padding: 32px;
                            box-shadow: 0 24px 60px rgba(77, 48, 28, 0.14);
                            backdrop-filter: blur(12px);
                        }

                        .eyebrow,
                        h1 {
                            font-family: "Palatino Linotype", Georgia, serif;
                        }

                        .eyebrow {
                            margin: 0 0 12px;
                            text-transform: uppercase;
                            letter-spacing: 0.16em;
                            font-size: 0.78rem;
                            color: var(--accent-deep);
                        }

                        h1 {
                            margin: 0;
                            font-size: 2.4rem;
                            line-height: 1.05;
                        }

                        p {
                            color: var(--muted);
                            line-height: 1.7;
                        }

                        label {
                            display: block;
                            margin-top: 16px;
                            font-size: 0.92rem;
                        }

                        span {
                            display: block;
                            margin-bottom: 8px;
                        }

                        input {
                            width: 100%;
                            border: 1px solid var(--line);
                            border-radius: 14px;
                            padding: 13px 14px;
                            font: inherit;
                            background: rgba(255, 255, 255, 0.9);
                            color: var(--ink);
                        }

                        .password-row {
                            position: relative;
                        }

                        .password-toggle {
                            position: absolute;
                            top: 50%;
                            right: 12px;
                            transform: translateY(-50%);
                            border: none;
                            background: transparent;
                            color: var(--accent-deep);
                            cursor: pointer;
                            padding: 4px 6px;
                            width: auto;
                            margin: 0;
                            display: inline-flex;
                            align-items: center;
                            justify-content: center;
                        }

                        .password-toggle svg {
                            width: 20px;
                            height: 20px;
                            stroke: currentColor;
                            fill: none;
                            stroke-width: 1.8;
                            stroke-linecap: round;
                            stroke-linejoin: round;
                        }

                        .password-toggle .eye-off {
                            display: none;
                        }

                        .password-toggle.is-visible .eye-open {
                            display: none;
                        }

                        .password-toggle.is-visible .eye-off {
                            display: block;
                        }

                        button {
                            width: 100%;
                            margin-top: 22px;
                            border: none;
                            border-radius: 999px;
                            padding: 14px 16px;
                            font: inherit;
                            color: white;
                            background: linear-gradient(90deg, var(--accent), var(--teal));
                            cursor: pointer;
                        }

                        .notice {
                            margin-top: 18px;
                            padding: 12px 14px;
                            border-radius: 14px;
                            font-size: 0.95rem;
                        }

                        .notice-info {
                            background: rgba(31, 79, 122, 0.08);
                            color: #1f4f7a;
                        }

                        .notice-error {
                            background: rgba(201, 92, 43, 0.12);
                            color: #7f3010;
                        }

                        .notice-success {
                            background: rgba(31, 122, 114, 0.12);
                            color: #145b55;
                        }

                        .footer-note {
                            margin-top: 20px;
                            font-size: 0.88rem;
                        }
                    </style>
                </head>
                <body>
                    <main class="login-card">
                        <p class="eyebrow">Protected Access</p>
                        <h1>Retail Dashboard</h1>
                        <p>Use your configured application credentials to enter the dashboard, call the APIs, and open Swagger.</p>
                        <div class="notice __MESSAGE_CLASS__" role="alert" aria-live="polite">__MESSAGE__</div>
                        <form method="post" action="/login">
                            __CSRF_FIELD__
                            <label>
                                <span>Username</span>
                                <input type="text" name="username" autocomplete="username" required>
                            </label>
                            <label>
                                <span>Password</span>
                                <div class="password-row">
                                    <input id="password" type="password" name="password" autocomplete="current-password" required>
                                    <button type="button" class="password-toggle" id="togglePassword" aria-label="Show password" title="Show password">
                                        <svg class="eye-open" viewBox="0 0 24 24" aria-hidden="true">
                                            <path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6-10-6-10-6z"></path>
                                            <circle cx="12" cy="12" r="3"></circle>
                                        </svg>
                                        <svg class="eye-off" viewBox="0 0 24 24" aria-hidden="true">
                                            <path d="M3 3l18 18"></path>
                                            <path d="M10.6 10.7a3 3 0 0 0 4 4"></path>
                                            <path d="M9.9 5.1A10.9 10.9 0 0 1 12 5c6.5 0 10 7 10 7a17.7 17.7 0 0 1-4 4.9"></path>
                                            <path d="M6.6 6.7A17.2 17.2 0 0 0 2 12s3.5 7 10 7a10.7 10.7 0 0 0 5.4-1.5"></path>
                                        </svg>
                                    </button>
                                </div>
                            </label>
                            <button type="submit">Sign In</button>
                        </form>
                        <p class="footer-note">Default local values live in <code>application.properties</code>. Override them with environment variables before deployment.</p>
                    </main>
                    <script>
                        const passwordInput = document.getElementById("password");
                        const togglePassword = document.getElementById("togglePassword");

                        togglePassword.addEventListener("click", () => {
                            const showingPassword = passwordInput.type === "text";
                            passwordInput.type = showingPassword ? "password" : "text";
                            togglePassword.classList.toggle("is-visible", !showingPassword);
                            togglePassword.setAttribute("aria-label", showingPassword ? "Show password" : "Hide password");
                            togglePassword.setAttribute("title", showingPassword ? "Show password" : "Hide password");
                        });
                    </script>
                </body>
                </html>
                """;

        String html = htmlTemplate
                .replace("__MESSAGE_CLASS__", messageClass)
                .replace("__MESSAGE__", message)
                .replace("__CSRF_FIELD__", csrfField);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}
