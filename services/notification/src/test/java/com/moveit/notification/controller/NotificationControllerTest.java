package com.moveit.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveit.notification.dto.*;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests pour NotificationController avec le nouveau modèle.
 */
@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    private NotificationResponse notificationResponse;
    private NotificationRequest notificationRequest;

    @BeforeEach
    void setUp() {
        notificationResponse = NotificationResponse.builder()
                .id(1L)
                .userId(123L)
                .type(NotificationType.SECURITY_INCIDENT)
                .typeName("SECURITY_INCIDENT")
                .levelName("CRITIQUE")
                .levelPriority(1)
                .name("Test notification")
                .body("Test body")
                .read(false)
                .mandatory(true)
                .build();

        notificationRequest = new NotificationRequest();
        notificationRequest.setUserId(123L);
        notificationRequest.setType(NotificationType.SECURITY_INCIDENT);
        notificationRequest.setLevelName("CRITIQUE");
        notificationRequest.setName("Test notification");
        notificationRequest.setBody("Test body");
    }

    @Test
    void getMyNotifications_shouldReturnNotificationList() throws Exception {
        // Given
        NotificationListResponse listResponse = NotificationListResponse.builder()
                .notifications(Arrays.asList(notificationResponse))
                .unreadCount(5L)
                .hasMore(false)
                .lastNotificationId(1L)
                .build();

        when(notificationService.getUserNotifications(eq(123L), isNull(), isNull(), eq(20), isNull()))
                .thenReturn(listResponse);

        // When & Then
        mockMvc.perform(get("/api/notifications")
                        .header("X-User-Id", "123")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications").isArray())
                .andExpect(jsonPath("$.notifications[0].id").value(1))
                .andExpect(jsonPath("$.unreadCount").value(5))
                .andExpect(jsonPath("$.hasMore").value(false));
    }

    @Test
    void getMyCriticalAlerts_shouldReturnCriticalAlerts() throws Exception {
        // Given
        List<NotificationResponse> alerts = Arrays.asList(notificationResponse);
        when(notificationService.getCriticalAlerts(123L)).thenReturn(alerts);

        // When & Then
        mockMvc.perform(get("/api/notifications/critical-alerts")
                        .header("X-User-Id", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].levelName").value("CRITIQUE"));
    }

    @Test
    void markAsRead_shouldUpdateRecipientStatus() throws Exception {
        // Given
        MarkAsReadRequest request = new MarkAsReadRequest(true);
        NotificationResponse updatedResponse = NotificationResponse.builder()
                .id(1L)
                .userId(123L)
                .read(true)
                .build();

        when(notificationService.markAsRead(1L, 123L, true)).thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(patch("/api/notifications/1/read")
                        .header("X-User-Id", "123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void markAllAsRead_shouldReturnNoContent() throws Exception {
        // Given
        when(notificationService.markAllAsRead(123L)).thenReturn(5);

        // When & Then
        mockMvc.perform(post("/api/notifications/mark-all-read")
                        .header("X-User-Id", "123"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getUnreadCount_shouldReturnCount() throws Exception {
        // Given
        when(notificationService.countUnreadNotifications(123L)).thenReturn(10L);

        // When & Then
        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("X-User-Id", "123"))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));
    }

    @Test
    void sendBroadcast_shouldCreateBroadcastNotification() throws Exception {
        // Given
        BroadcastNotificationRequest broadcastRequest = new BroadcastNotificationRequest();
        broadcastRequest.setType(NotificationType.EMERGENCY_ALERT);
        broadcastRequest.setLevelName("CRITIQUE");
        broadcastRequest.setName("Emergency");
        broadcastRequest.setBody("Evacuate now");
        broadcastRequest.setTargetUserIds(Set.of(100L, 200L, 300L));

        // When & Then
        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(broadcastRequest)))
                .andExpect(status().isAccepted());
    }

    @Test
    void sendBroadcast_shouldAcceptSubscriptionBasedBroadcast() throws Exception {
        // Given
        BroadcastNotificationRequest broadcastRequest = new BroadcastNotificationRequest();
        broadcastRequest.setType(NotificationType.SECURITY_INCIDENT);
        broadcastRequest.setLevelName("CRITIQUE");
        broadcastRequest.setName("Security Alert");
        broadcastRequest.setBody("Check your surroundings");
        broadcastRequest.setTopic("SECURITY");
        // Pas de targetUserIds = utilise les subscriptions

        // When & Then
        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(broadcastRequest)))
                .andExpect(status().isAccepted());
    }

    @Test
    void sendBroadcast_shouldAcceptGlobalBroadcast() throws Exception {
        // Given
        BroadcastNotificationRequest broadcastRequest = new BroadcastNotificationRequest();
        broadcastRequest.setType(NotificationType.GENERAL_INFO);
        broadcastRequest.setLevelName("ORGANISATIONNEL");
        broadcastRequest.setName("General Announcement");
        broadcastRequest.setBody("Event starting soon");
        broadcastRequest.setBroadcast(true); // Broadcast à tous

        // When & Then
        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(broadcastRequest)))
                .andExpect(status().isAccepted());
    }
}

