package com.moveit.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveit.notification.dto.SubscribeRequest;
import com.moveit.notification.dto.SubscriptionResponse;
import com.moveit.notification.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests pour SubscriptionController.
 */
@WebMvcTest(SubscriptionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubscriptionService subscriptionService;

    private SubscriptionResponse subscriptionResponse;
    private SubscribeRequest subscribeRequest;

    @BeforeEach
    void setUp() {
        subscriptionResponse = SubscriptionResponse.builder()
                .id(1L)
                .userId(123L)
                .typeName("SECURITY_INCIDENT")
                .levelName("CRITIQUE")
                .topic(null)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        subscribeRequest = new SubscribeRequest();
        subscribeRequest.setTypeName("SECURITY_INCIDENT");
        subscribeRequest.setLevelName("CRITIQUE");
        subscribeRequest.setTopic(null);
        subscribeRequest.setActive(true);
    }

    @Test
    void subscribe_shouldCreateNewSubscription() throws Exception {
        // Given
        when(subscriptionService.subscribe(eq(123L), any(SubscribeRequest.class)))
                .thenReturn(subscriptionResponse);

        // When & Then
        mockMvc.perform(post("/api/notifications/subscriptions")
                        .header("X-User-Id", "123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subscribeRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(123))
                .andExpect(jsonPath("$.typeName").value("SECURITY_INCIDENT"))
                .andExpect(jsonPath("$.levelName").value("CRITIQUE"))
                .andExpect(jsonPath("$.active").value(true));

        verify(subscriptionService).subscribe(eq(123L), any(SubscribeRequest.class));
    }

    @Test
    void subscribe_shouldAllowBroadSubscription() throws Exception {
        // Given
        SubscribeRequest broadRequest = new SubscribeRequest();
        broadRequest.setTypeName(null); // S'abonner à tous les types
        broadRequest.setLevelName(null); // Tous les niveaux
        broadRequest.setActive(true);

        SubscriptionResponse broadResponse = SubscriptionResponse.builder()
                .id(2L)
                .userId(123L)
                .typeName(null)
                .levelName(null)
                .topic(null)
                .active(true)
                .createdAt(Instant.now())
                .build();

        when(subscriptionService.subscribe(eq(123L), any(SubscribeRequest.class)))
                .thenReturn(broadResponse);

        // When & Then
        mockMvc.perform(post("/api/notifications/subscriptions")
                        .header("X-User-Id", "123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(broadRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.typeName").doesNotExist())
                .andExpect(jsonPath("$.levelName").doesNotExist());
    }

    @Test
    void getMySubscriptions_shouldReturnSubscriptionList() throws Exception {
        // Given
        List<SubscriptionResponse> subscriptions = Arrays.asList(subscriptionResponse);
        when(subscriptionService.getUserSubscriptions(123L)).thenReturn(subscriptions);

        // When & Then
        mockMvc.perform(get("/api/notifications/subscriptions")
                        .header("X-User-Id", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].userId").value(123))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(subscriptionService).getUserSubscriptions(123L);
    }

    @Test
    void unsubscribe_shouldDeactivateSubscription() throws Exception {
        // Given
        doNothing().when(subscriptionService).unsubscribe(123L, 1L);

        // When & Then
        mockMvc.perform(patch("/api/notifications/subscriptions/1/unsubscribe")
                        .header("X-User-Id", "123"))
                .andExpect(status().isNoContent());

        verify(subscriptionService).unsubscribe(123L, 1L);
    }

    @Test
    void unsubscribe_shouldReturnBadRequest_whenMandatoryType() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Cannot unsubscribe from mandatory notification type"))
                .when(subscriptionService).unsubscribe(123L, 1L);

        // When & Then
        mockMvc.perform(patch("/api/notifications/subscriptions/1/unsubscribe")
                        .header("X-User-Id", "123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void subscribe_shouldReturnBadRequest_whenUserIdHeaderMissing() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/notifications/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subscribeRequest)))
                .andExpect(status().isBadRequest());

        verify(subscriptionService, never()).subscribe(any(), any());
    }
}
