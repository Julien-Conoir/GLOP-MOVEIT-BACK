package com.moveit.notification.service.impl;

import com.moveit.notification.dto.*;
import com.moveit.notification.entity.Notification;
import com.moveit.notification.entity.NotificationLevel;
import com.moveit.notification.entity.NotificationRecipient;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.repository.NotificationLevelRepository;
import com.moveit.notification.repository.NotificationRecipientRepository;
import com.moveit.notification.repository.NotificationRepository;
import com.moveit.notification.repository.NotificationSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests pour NotificationServiceImpl avec le nouveau modèle notification+recipient.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationRecipientRepository notificationRecipientRepository;

    @Mock
    private NotificationSubscriptionRepository notificationSubscriptionRepository;

    @Mock
    private NotificationLevelRepository notificationLevelRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private NotificationLevel criticalLevel;
    private Notification notification;
    private NotificationRecipient recipient;
    private NotificationRequest request;
    private NotificationResponse response;

    @BeforeEach
    void setUp() {
        criticalLevel = new NotificationLevel();
        criticalLevel.setId(1);
        criticalLevel.setName("CRITIQUE");
        criticalLevel.setPriority(1);

        notification = Notification.builder()
                .id(1L)
                .type(NotificationType.SECURITY_INCIDENT)
                .level(criticalLevel)
                .name("Test notification")
                .body("Test body")
                .startDate(Instant.now())
                .build();

        recipient = new NotificationRecipient();
        recipient.setId(1L);
        recipient.setNotification(notification);
        recipient.setUserId(123L);
        recipient.setRead(false);

        request = new NotificationRequest();
        request.setUserId(123L);
        request.setType(NotificationType.SECURITY_INCIDENT);
        request.setLevelName("CRITIQUE");
        request.setName("Test notification");
        request.setBody("Test body");

        response = NotificationResponse.builder()
                .id(1L)
                .userId(123L)
                .type(NotificationType.SECURITY_INCIDENT)
                .levelName("CRITIQUE")
                .name("Test notification")
                .body("Test body")
                .read(false)
                .build();
    }

    @Test
    void createNotification_shouldCreateNotificationAndRecipient() {
        // Given
        when(notificationLevelRepository.findByName("CRITIQUE")).thenReturn(Optional.of(criticalLevel));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationRecipientRepository.save(any(NotificationRecipient.class))).thenReturn(recipient);
        when(notificationMapper.toResponse(recipient)).thenReturn(response);

        // When
        NotificationResponse result = notificationService.createNotification(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(123L);
        assertThat(result.getType()).isEqualTo(NotificationType.SECURITY_INCIDENT);
        verify(notificationRepository).save(any(Notification.class));
        verify(notificationRecipientRepository).save(any(NotificationRecipient.class));
    }

    @Test
    void createNotification_shouldThrowException_whenLevelNotFound() {
        // Given
        when(notificationLevelRepository.findByName("CRITIQUE")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> notificationService.createNotification(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid level name");

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void getUserNotifications_shouldReturnNotificationList() {
        // Given
        List<NotificationRecipient> recipients = Arrays.asList(recipient);
        Page<NotificationRecipient> page = new PageImpl<>(recipients);

        when(notificationRecipientRepository.findByUserIdWithFilters(
                eq(123L), eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(page);
        when(notificationRecipientRepository.countByUserIdAndReadFalse(123L)).thenReturn(5L);
        when(notificationMapper.toListResponse(anyList(), eq(5L), eq(false)))
                .thenReturn(NotificationListResponse.builder()
                        .notifications(List.of(response))
                        .unreadCount(5L)
                        .hasMore(false)
                        .build());

        // When
        NotificationListResponse result = notificationService.getUserNotifications(123L, null, null, 20, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUnreadCount()).isEqualTo(5L);
        assertThat(result.isHasMore()).isFalse();
        verify(notificationRecipientRepository).findByUserIdWithFilters(
                eq(123L), eq(null), eq(null), eq(null), any(Pageable.class));
    }

    @Test
    void getCriticalAlerts_shouldReturnCriticalNotifications() {
        // Given
        List<NotificationRecipient> criticalRecipients = Arrays.asList(recipient);
        when(notificationRecipientRepository.findCriticalUnreadByUserId(123L)).thenReturn(criticalRecipients);
        when(notificationMapper.toResponse(recipient)).thenReturn(response);

        // When
        List<NotificationResponse> result = notificationService.getCriticalAlerts(123L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLevelName()).isEqualTo("CRITIQUE");
        verify(notificationRecipientRepository).findCriticalUnreadByUserId(123L);
    }

    @Test
    void markAsRead_shouldUpdateRecipientStatus() {
        // Given
        when(notificationRecipientRepository.findByIdAndUserId(1L, 123L)).thenReturn(Optional.of(recipient));
        when(notificationRecipientRepository.save(recipient)).thenReturn(recipient);
        when(notificationMapper.toResponse(recipient)).thenReturn(response);

        // When
        NotificationResponse result = notificationService.markAsRead(1L, 123L, true);

        // Then
        assertThat(result).isNotNull();
        assertThat(recipient.getRead()).isTrue();
        assertThat(recipient.getReadAt()).isNotNull();
        verify(notificationRecipientRepository).save(recipient);
    }

    @Test
    void markAsRead_shouldThrowException_whenRecipientNotFound() {
        // Given
        when(notificationRecipientRepository.findByIdAndUserId(999L, 123L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> notificationService.markAsRead(999L, 123L, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Notification recipient not found");

        verify(notificationRecipientRepository, never()).save(any());
    }

    @Test
    void markAllAsRead_shouldUseModifyingQuery() {
        // Given
        when(notificationRecipientRepository.markAllAsReadByUserId(123L)).thenReturn(5);

        // When
        int count = notificationService.markAllAsRead(123L);

        // Then
        assertThat(count).isEqualTo(5);
        verify(notificationRecipientRepository).markAllAsReadByUserId(123L);
    }

    @Test
    void countUnreadNotifications_shouldReturnCorrectCount() {
        // Given
        when(notificationRecipientRepository.countByUserIdAndReadFalse(123L)).thenReturn(10L);

        // When
        long count = notificationService.countUnreadNotifications(123L);

        // Then
        assertThat(count).isEqualTo(10L);
        verify(notificationRecipientRepository).countByUserIdAndReadFalse(123L);
    }

    // ========== Tests pour sendBroadcast ==========

    @Test
    void sendBroadcast_withExplicitUserIds_shouldCreateRecipientsForSpecifiedUsers() {
        // Given
        BroadcastNotificationRequest broadcastRequest = new BroadcastNotificationRequest();
        broadcastRequest.setType(NotificationType.SECURITY_INCIDENT);
        broadcastRequest.setLevelName("CRITIQUE");
        broadcastRequest.setName("Broadcast test");
        broadcastRequest.setBody("Test body");
        broadcastRequest.setTargetUserIds(Set.of(100L, 200L, 300L));

        when(notificationLevelRepository.findByName("CRITIQUE")).thenReturn(Optional.of(criticalLevel));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationRecipientRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // When
        notificationService.sendBroadcast(broadcastRequest);

        // Then
        verify(notificationRepository).save(any(Notification.class));
        verify(notificationRecipientRepository, atLeastOnce()).saveAll(argThat(list -> {
            if (list == null) return false;
            if (list instanceof List) {
                return ((List<?>) list).size() <= 3;
            }
            return true;
        }));
        verify(notificationRecipientRepository, atLeastOnce()).flush();
    }

    @Test
    void sendBroadcast_withSubscriptions_shouldResolveSubscribedUsers() {
        // Given
        BroadcastNotificationRequest broadcastRequest = new BroadcastNotificationRequest();
        broadcastRequest.setType(NotificationType.SECURITY_INCIDENT);
        broadcastRequest.setLevelName("CRITIQUE");
        broadcastRequest.setName("Subscription broadcast");
        broadcastRequest.setBody("Test body");
        broadcastRequest.setTopic("SECURITY");
        // Pas de targetUserIds ni broadcast = utilise subscriptions

        Set<Long> subscribedUsers = Set.of(100L, 200L);
        when(notificationSubscriptionRepository.findSubscribedUserIds("SECURITY_INCIDENT", "CRITIQUE", "SECURITY"))
                .thenReturn(subscribedUsers);
        when(notificationLevelRepository.findByName("CRITIQUE")).thenReturn(Optional.of(criticalLevel));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationRecipientRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // When
        notificationService.sendBroadcast(broadcastRequest);

        // Then
        verify(notificationSubscriptionRepository).findSubscribedUserIds("SECURITY_INCIDENT", "CRITIQUE", "SECURITY");
        verify(notificationRecipientRepository, atLeastOnce()).saveAll(argThat(list -> {
            if (list == null) return false;
            if (list instanceof List) {
                return ((List<?>) list).size() <= 2;
            }
            return true;
        }));
    }

    @Test
    void sendBroadcast_shouldThrowException_whenLevelNotFound() {
        // Given
        BroadcastNotificationRequest broadcastRequest = new BroadcastNotificationRequest();
        broadcastRequest.setType(NotificationType.SECURITY_INCIDENT);
        broadcastRequest.setLevelName("INVALID_LEVEL");
        broadcastRequest.setName("Test");
        broadcastRequest.setBody("Test");
        broadcastRequest.setTargetUserIds(Set.of(100L));

        when(notificationLevelRepository.findByName("INVALID_LEVEL")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> notificationService.sendBroadcast(broadcastRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Level not found");

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void sendBroadcast_shouldHandleBatchInserts_whenManyRecipients() {
        // Given - Créer plus de 1000 userIds pour tester le batching
        Set<Long> manyUserIds = new HashSet<>();
        for (long i = 1; i <= 2500; i++) {
            manyUserIds.add(i);
        }

        BroadcastNotificationRequest broadcastRequest = new BroadcastNotificationRequest();
        broadcastRequest.setType(NotificationType.EMERGENCY_ALERT);
        broadcastRequest.setLevelName("CRITIQUE");
        broadcastRequest.setName("Emergency");
        broadcastRequest.setBody("Evacuate now");
        broadcastRequest.setTargetUserIds(manyUserIds);

        when(notificationLevelRepository.findByName("CRITIQUE")).thenReturn(Optional.of(criticalLevel));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationRecipientRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // When
        notificationService.sendBroadcast(broadcastRequest);

        // Then - Doit appeler saveAll au moins 3 fois (2500 / 1000 = 3 batches)
        verify(notificationRecipientRepository, atLeast(3)).saveAll(anyList());
        verify(notificationRecipientRepository, atLeast(3)).flush();
    }
}
