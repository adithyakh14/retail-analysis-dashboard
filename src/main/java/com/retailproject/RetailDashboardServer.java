package com.retailproject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RetailDashboardServer {
    public static void main(String[] args) throws IOException {
        int port = resolvePort();
        DashboardDataService dataService = new DashboardDataService();
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);

        server.createContext("/api/dashboard", exchange -> handleJson(exchange, dataService.getDashboardData(queryParams(exchange.getRequestURI()))));
        server.createContext("/api/filter-options", exchange -> handleJson(exchange, dataService.getFilterOptions()));
        server.createContext("/api/table", exchange -> handleJson(exchange, dataService.getTable(
                queryParams(exchange.getRequestURI()).getOrDefault("name", "raw-input"),
                queryParams(exchange.getRequestURI()))));
        server.createContext("/api/ask", exchange -> {
            Map<String, String> params = queryParams(exchange.getRequestURI());
            handleJson(exchange, dataService.askQuestion(params.getOrDefault("q", ""), params));
        });
        server.createContext("/download", exchange -> handleDownload(exchange, dataService));
        server.createContext("/", RetailDashboardServer::handleStatic);

        server.setExecutor(null);
        server.start();
        System.out.println("Retail dashboard server started on port " + port);
    }

    private static void handleJson(HttpExchange exchange, Map<String, Object> payload) throws IOException {
        byte[] body = JsonUtil.toJson(payload).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(body);
        }
    }

    private static void handleDownload(HttpExchange exchange, DashboardDataService dataService) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String name = path.substring("/download/".length());
        try {
            DashboardDataService.DownloadFile file = dataService.getDownloadFile(name);
            exchange.getResponseHeaders().set("Content-Type", "text/csv; charset=UTF-8");
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + file.fileName() + "\"");
            exchange.sendResponseHeaders(200, file.content().length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(file.content());
            }
        } catch (IllegalArgumentException exception) {
            sendText(exchange, 404, "File not found.");
        }
    }

    private static void handleStatic(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String resourcePath = switch (path) {
            case "/" -> "web/index.html";
            case "/styles.css" -> "web/styles.css";
            case "/app.js" -> "web/app.js";
            default -> null;
        };

        if (resourcePath == null) {
            sendText(exchange, 404, "Not found");
            return;
        }

        try (InputStream inputStream = RetailDashboardServer.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                sendText(exchange, 404, "Not found");
                return;
            }

            byte[] body = inputStream.readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", contentType(resourcePath));
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(body);
            }
        }
    }

    private static void sendText(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, body.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(body);
        }
    }

    private static Map<String, String> queryParams(URI uri) {
        Map<String, String> params = new HashMap<>();
        String query = uri.getRawQuery();
        if (query == null || query.isBlank()) {
            return params;
        }

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            params.put(key, value);
        }
        return params;
    }

    private static String contentType(String resourcePath) {
        if (resourcePath.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }
        if (resourcePath.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        }
        return "text/html; charset=UTF-8";
    }

    private static int resolvePort() {
        String portValue = System.getenv("PORT");
        if (portValue == null || portValue.isBlank()) {
            return 8080;
        }

        try {
            return Integer.parseInt(portValue.trim());
        } catch (NumberFormatException exception) {
            return 8080;
        }
    }
}
