package com.moveit.notification.service.impl;

import com.moveit.notification.dto.NotificationListResponse;
import com.moveit.notification.dto.NotificationMapper;
import com.moveit.notification.dto.NotificationRequest;
import com.moveit.notification.dto.NotificationResponse;
import com.moveit.notification.entity.Notification;
import com.moveit.notification.entity.NotificationLevel;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.repository.NotificationLevelRepository;
import com.moveit.notification.repository.NotificationRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationLevelRepository notificationLevelRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private NotificationLevel criticalLevel;
    private Notification notification;
    private NotificationRequest request;
    private NotificationResponse response;

    @BeforeEach
    void setUp() {
        criticalLevel = new NotificationLevel();
        criticalLevel.setId(1);
        criticalLevel.setName("CRITIQUE");
        criticalLevel.setPriority(1);

        notification = new Notification();
        notification.setId(1L);
        notification.setUserId(123L);
        notification.setType(NotificationType.SECURITY_INCIDENT);
        notification.setLevel(criticalLevel);
        notification.setName("Test notification");
        notification.setBody("Test body");
        notification.setRead(false);
        notification.setStartDate(Instant.now());

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
    void createNotification_shouldCreateAndReturnNotification() {
        // Given
        when(notificationLevelRepository.findByName("CRITIQUE")).thenReturn(Optional.of(criticalLevel));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toResponse(notification)).thenReturn(response);

        // When
        NotificationResponse result = notificationService.createNotification(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(123L);
        assertThat(result.getType()).isEqualTo(NotificationType.SECURITY_INCIDENT);
        verify(notificationRepository).save(any(Notification.class));
        verify(notificationMapper).toResponse(notification);
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
        List<Notification> notifications = Arrays.asList(notification);
        Page<Notification> page = new PageImpl<>(notifications);

        when(notificationRepository.findByUserIdWithFilters(eq(123L), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(page);
        when(notificationRepository.countByUserIdAndReadFalse(123L)).thenReturn(5L);
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
        verify(notificationRepository).findByUserIdWithFilters(eq(123L), eq(null), eq(null), any(Pageable.class));
    }

    @Test
    void getCriticalAlerts_shouldReturnCriticalNotifications() {
        // Given
        List<Notification> criticalNotifs = Arrays.asList(notification);
        when(notificationRepository.findCriticalUnreadByUserId(123L)).thenReturn(criticalNotifs);
        when(notificationMapper.toResponse(notification)).thenReturn(response);

        // When
        List<NotificationResponse> result = notificationService.getCriticalAlerts(123L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLevelName()).isEqualTo("CRITIQUE");
        verify(notificationRepository).findCriticalUnreadByUserId(123L);
    }

    @Test
    void markAsRead_shouldUpdateNotificationStatus() {
        // Given
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);
        when(notificationMapper.toResponse(notification)).thenReturn(response);

        // When
        NotificationResponse result = notificationService.markAsRead(1L, true);

        // Then
        assertThat(result).isNotNull();
        assertThat(notification.getRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_shouldThrowException_whenNotificationNotFound() {
        // Given
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> notificationService.markAsRead(999L, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Notification not found");

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAllAsRead_shouldMarkAllUnreadNotifications() {
        // Given
        Notification notif1 = new Notification();
        notif1.setRead(false);
        Notification notif2 = new Notification();
        notif2.setRead(false);

        List<Notification> unreadNotifs = Arrays.asList(notif1, notif2);
        Page<Notification> page = new PageImpl<>(unreadNotifs);

        when(notificationRepository.findByUserIdWithFilters(eq(123L), eq(null), eq(false), any(Pageable.class)))
                .thenReturn(page);

        // When
        int count = notificationService.markAllAsRead(123L);

        // Then
        assertThat(count).isEqualTo(2);
        assertThat(notif1.getRead()).isTrue();
        assertThat(notif2.getRead()).isTrue();
        verify(notificationRepository).saveAll(unreadNotifs);
    }

    @Test
    void countUnreadNotifications_shouldReturnCorrectCount() {
        // Given
        when(notificationRepository.countByUserIdAndReadFalse(123L)).thenReturn(10L);

        // When
        long count = notificationService.countUnreadNotifications(123L);

        // Then
        assertThat(count).isEqualTo(10L);
        verify(notificationRepository).countByUserIdAndReadFalse(123L);
    }
}
