package com.moveit.notification.security;

import com.moveit.notification.entity.Notification;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.repository.NotificationRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Notification Service Security Integration Tests")
class NotificationServiceSecurityTest {

    @LocalServerPort
    private int port;

    @Autowired
    private NotificationRepository notificationRepository;

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
        
        // Nettoyer la base de données
        notificationRepository.deleteAll();
        
        // Créer une notification de test
        Notification notification = new Notification();
        notification.setTitle("Test Title");
        notification.setContent("Test notification content");
        notification.setNotificationType(NotificationType.SYSTEM);
        notification.setIncidentIds(new java.util.HashSet<>());
        notification.setEventIds(new java.util.HashSet<>());
        notificationRepository.save(notification);
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
    @DisplayName("GET /notifications without token should return 401")
    void testGetNotifications_NoToken_Returns401() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/notifications"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(401, response.statusCode());
    }

    @Test
    @DisplayName("GET /notifications with valid token should return 200")
    void testGetNotifications_WithValidToken_Returns200() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/notifications?page=0&size=10"))
                .header("Authorization", "Bearer " + validToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            System.out.println("Response status: " + response.statusCode());
            System.out.println("Response body: " + response.body());
        }
        assertEquals(200, response.statusCode());
    }

    @Test
    @DisplayName("GET /notifications with invalid token should return 401")
    void testGetNotifications_WithInvalidToken_Returns401() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/notifications"))
                .header("Authorization", "Bearer invalid.token.here")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(401, response.statusCode());
    }

    @Test
    @DisplayName("GET /notifications with expired token should return 401")
    void testGetNotifications_WithExpiredToken_Returns401() throws IOException, InterruptedException {
        String expiredToken = createToken("testuser", System.currentTimeMillis() - 3600000);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/notifications"))
                .header("Authorization", "Bearer " + expiredToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(401, response.statusCode());
    }

    @Test
    @DisplayName("POST /notifications without token should return 401")
    void testCreateNotification_NoToken_Returns401() throws IOException, InterruptedException {
        String json = """
                {
                    "title": "Test Title",
                    "content": "Test notification content",
                    "notificationType": "SYSTEM"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/notifications"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(401, response.statusCode());
    }

    @Test
    @DisplayName("POST /notifications with valid token should succeed")
    void testCreateNotification_WithValidToken_ReturnsSuccess() throws IOException, InterruptedException {
        String json = """
                {
                    "title": "Test Title",
                    "content": "Test notification content",
                    "notificationType": "SYSTEM"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/notifications"))
                .header("Authorization", "Bearer " + validToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
    }

    @Test
    @DisplayName("Actuator endpoints should be accessible without token")
    void testActuatorEndpoint_NoToken_Returns200() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/actuator/health"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
    }
}
