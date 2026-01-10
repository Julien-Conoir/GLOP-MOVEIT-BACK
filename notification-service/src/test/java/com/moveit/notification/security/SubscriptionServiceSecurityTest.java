package com.moveit.notification.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Subscription Service Security Integration Tests")
class SubscriptionServiceSecurityTest {

    @LocalServerPort
    private int port;

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    private HttpClient httpClient;
    private String baseUrl;
    private String validToken;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newHttpClient();
        baseUrl = "http://localhost:" + port + "/api/notification";
        validToken = createToken("testuser", System.currentTimeMillis() + 3600000);
    }

    private String createToken(String username, long expirationTime) {
        SecretKey key = Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(secretKey));
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(expirationTime))
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("GET /subscriptions without token should return 401")
    void testGetSubscriptions_NoToken_Returns401() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/subscriptions"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(401, response.statusCode());
    }

    @Test
    @DisplayName("GET /subscriptions with valid token should return 200")
    void testGetSubscriptions_WithValidToken_Returns200() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/subscriptions"))
                .header("Authorization", "Bearer " + validToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
    }
}
