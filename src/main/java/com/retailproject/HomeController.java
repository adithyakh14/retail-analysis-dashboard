package com.retailproject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {
    private final String frontendUrl;

    public HomeController(@Value("${app.frontend-url:}") String frontendUrl) {
        this.frontendUrl = frontendUrl == null ? "" : frontendUrl.trim();
    }

    @GetMapping("/")
    @ResponseBody
    public ResponseEntity<String> home() {
        if (!frontendUrl.isBlank()) {
            return ResponseEntity.status(302)
                    .header(HttpHeaders.LOCATION, frontendUrl)
                    .build();
        }

        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Retail Analysis Dashboard API</title>
                    <style>
                        body {
                            margin: 0;
                            font-family: Arial, sans-serif;
                            background: #f6f0e8;
                            color: #1f2430;
                        }
                        .wrap {
                            max-width: 840px;
                            margin: 60px auto;
                            padding: 32px;
                            background: #fffaf3;
                            border: 1px solid #e9dccd;
                            border-radius: 20px;
                            box-shadow: 0 20px 60px rgba(77, 48, 28, 0.10);
                        }
                        h1 {
                            margin-top: 0;
                            font-size: 2.4rem;
                        }
                        p {
                            line-height: 1.7;
                            color: #575d69;
                        }
                        ul {
                            line-height: 1.8;
                        }
                        a {
                            color: #c95c2b;
                            text-decoration: none;
                        }
                        a:hover {
                            text-decoration: underline;
                        }
                        code {
                            background: #f2e7db;
                            padding: 2px 6px;
                            border-radius: 6px;
                        }
                    </style>
                </head>
                <body>
                    <main class="wrap">
                        <h1>Retail Analysis Dashboard API</h1>
                        <p>This URL is the backend API service, not the standalone frontend dashboard.</p>
                        <p>The service is running correctly. Use one of the links below to explore it:</p>
                        <ul>
                            <li><a href="/swagger-ui.html">Swagger UI</a></li>
                            <li><a href="/api/filter-options">Sample API endpoint</a></li>
                            <li><a href="/api/dashboard">Dashboard API response</a></li>
                        </ul>
                        <p>If you want the domain root to open the dashboard automatically, set the backend property
                        <code>app.frontend-url</code> to the deployed frontend URL.</p>
                    </main>
                </body>
                </html>
                """;

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}
