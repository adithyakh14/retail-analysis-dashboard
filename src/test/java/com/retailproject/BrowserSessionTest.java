package com.retailproject;

import static org.junit.jupiter.api.Assertions.*;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BrowserSessionTest {
    @LocalServerPort
    private int port;

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private HttpResponse<String> get(HttpClient client, String path, String accept) throws Exception {
        return client.send(HttpRequest.newBuilder(uri(path)).header("Accept", accept).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(HttpClient client, String path, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "text/html")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    @ParameterizedTest
    @CsvSource({"admin,change-me-now,true", "analyst,change-me-user,false"})
    void browserLoginSwaggerAndLogout(String username, String password, boolean admin) throws Exception {
        CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        HttpClient client = HttpClient.newBuilder().cookieHandler(cookies)
                .followRedirects(HttpClient.Redirect.NEVER).build();
        HttpResponse<String> login = get(client, "/login", "text/html");
        assertEquals(200, login.statusCode());
        var tokenMatch = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"").matcher(login.body());
        assertTrue(tokenMatch.find());
        HttpResponse<String> signedIn = post(client, "/login", "username=" + username + "&password=" + password
                + "&_csrf=" + URLEncoder.encode(tokenMatch.group(1), StandardCharsets.UTF_8));
        assertEquals(302, signedIn.statusCode());
        assertTrue(signedIn.headers().firstValue("location").orElseThrow().endsWith("/dashboard/index.html"));
        assertEquals(200, get(client, "/dashboard/index.html", "text/html").statusCode());

        HttpResponse<String> swagger = get(client, "/swagger-ui.html", "text/html");
        if (admin) {
            assertEquals(302, swagger.statusCode());
            assertEquals(200, get(client, "/swagger-ui/index.html", "text/html").statusCode());
            assertEquals(200, get(client, "/v3/api-docs", "application/json").statusCode());
        } else {
            assertEquals(403, swagger.statusCode());
            assertTrue(swagger.body().contains("Access restricted"));
            assertTrue(swagger.body().contains("Back to dashboard"));
            assertFalse(swagger.body().contains("Whitelabel"));
            assertEquals(403, get(client, "/swagger-ui/index.html", "text/html").statusCode());
            HttpResponse<String> docs = get(client, "/v3/api-docs", "application/json");
            assertEquals(403, docs.statusCode());
            assertTrue(docs.headers().firstValue("content-type").orElseThrow().contains("application/json"));
        }

        assertEquals(403, post(client, "/logout", "").statusCode());
        assertEquals(403, post(client, "/logout", "_csrf=invalid").statusCode());
        assertEquals(200, get(client, "/dashboard/index.html", "text/html").statusCode());
        HttpResponse<String> csrf = get(client, "/dashboard/csrf", "application/json");
        assertEquals(200, csrf.statusCode());
        assertTrue(csrf.headers().firstValue("cache-control").orElseThrow().contains("no-store"));
        var token = new ObjectMapper().readTree(csrf.body());
        String sessionCookie = cookies.getCookieStore().getCookies().stream()
                .filter(cookie -> cookie.getName().equals("JSESSIONID")).findFirst().orElseThrow().getValue();
        HttpResponse<String> logout = post(client, "/logout", token.get("parameterName").asText() + "="
                + URLEncoder.encode(token.get("token").asText(), StandardCharsets.UTF_8));
        assertEquals(302, logout.statusCode());
        assertTrue(logout.headers().firstValue("location").orElseThrow().endsWith("/login?logout=true"));
        assertTrue(get(client, "/login?logout=true", "text/html").body().contains("You have been signed out."));
        HttpResponse<String> replay = HttpClient.newHttpClient().send(HttpRequest.newBuilder(uri("/dashboard/index.html"))
                .header("Cookie", "JSESSIONID=" + sessionCookie).header("Accept", "text/html").GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(302, replay.statusCode());
        assertTrue(replay.headers().firstValue("location").orElseThrow().endsWith("/login"));
    }
}
