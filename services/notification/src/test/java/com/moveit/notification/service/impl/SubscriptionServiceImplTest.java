package com.moveit.notification.service.impl;

import com.moveit.notification.dto.SubscribeRequest;
import com.moveit.notification.dto.SubscriptionResponse;
import com.moveit.notification.entity.NotificationSubscription;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.repository.NotificationSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests pour SubscriptionServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock
    private NotificationSubscriptionRepository subscriptionRepository;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private NotificationSubscription subscription;
    private SubscribeRequest request;
    private Long userId = 123L;

    @BeforeEach
    void setUp() {
        subscription = new NotificationSubscription();
        subscription.setId(1L);
        subscription.setUserId(userId);
        subscription.setTypeName("GENERAL_INFO"); // Type non-mandatory
        subscription.setLevelName("CRITIQUE");
        subscription.setTopic(null);
        subscription.setActive(true);

        request = new SubscribeRequest();
        request.setTypeName("GENERAL_INFO"); // Type non-mandatory
        request.setLevelName("CRITIQUE");
        request.setActive(true);
    }

    @Test
    void subscribe_shouldCreateNewSubscription() {
        // Given
        when(subscriptionRepository.save(any(NotificationSubscription.class))).thenReturn(subscription);

        // When
        SubscriptionResponse result = subscriptionService.subscribe(userId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getTypeName()).isEqualTo("GENERAL_INFO");
        assertThat(result.getActive()).isTrue();
        verify(subscriptionRepository).save(any(NotificationSubscription.class));
    }

    @Test
    void subscribe_shouldAllowNullFilters() {
        // Given
        SubscribeRequest broadRequest = new SubscribeRequest();
        broadRequest.setTypeName(null); // S'abonner à tous les types
        broadRequest.setLevelName(null); // Tous les niveaux
        broadRequest.setTopic(null);
        broadRequest.setActive(true);

        NotificationSubscription broadSubscription = new NotificationSubscription();
        broadSubscription.setId(2L);
        broadSubscription.setUserId(userId);
        broadSubscription.setTypeName(null);
        broadSubscription.setLevelName(null);
        broadSubscription.setTopic(null);
        broadSubscription.setActive(true);

        when(subscriptionRepository.save(any(NotificationSubscription.class))).thenReturn(broadSubscription);

        // When
        SubscriptionResponse result = subscriptionService.subscribe(userId, broadRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTypeName()).isNull();
        assertThat(result.getLevelName()).isNull();
        verify(subscriptionRepository).save(any(NotificationSubscription.class));
    }

    @Test
    void getUserSubscriptions_shouldReturnActiveSubscriptions() {
        // Given
        List<NotificationSubscription> subscriptions = Arrays.asList(subscription);
        when(subscriptionRepository.findByUserIdAndActiveTrue(userId)).thenReturn(subscriptions);

        // When
        List<SubscriptionResponse> result = subscriptionService.getUserSubscriptions(userId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(userId);
        verify(subscriptionRepository).findByUserIdAndActiveTrue(userId);
    }

    @Test
    void unsubscribe_shouldDeactivateSubscription() {
        // Given
        when(subscriptionRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(subscription)).thenReturn(subscription);

        // When
        subscriptionService.unsubscribe(userId, 1L);

        // Then
        assertThat(subscription.getActive()).isFalse();
        verify(subscriptionRepository).save(subscription);
    }

    @Test
    void unsubscribe_shouldThrowException_whenMandatoryType() {
        // Given
        subscription.setTypeName("EMERGENCY_ALERT"); // Type mandatory
        when(subscriptionRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(subscription));

        // When & Then
        assertThatThrownBy(() -> subscriptionService.unsubscribe(userId, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot unsubscribe from mandatory notification type");

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void unsubscribe_shouldThrowException_whenSubscriptionNotFound() {
        // Given
        when(subscriptionRepository.findByIdAndUserId(999L, userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> subscriptionService.unsubscribe(userId, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Subscription not found");

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void unsubscribe_shouldThrowException_whenUserDoesNotOwnSubscription() {
        // Given
        when(subscriptionRepository.findByIdAndUserId(1L, 999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> subscriptionService.unsubscribe(999L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Subscription not found");
    }

    @Test
    void deleteSubscription_shouldDeleteSubscription() {
        // Given
        when(subscriptionRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(subscription));

        // When
        subscriptionService.deleteSubscription(userId, 1L);

        // Then
        verify(subscriptionRepository).delete(subscription);
    }

    @Test
    void deleteSubscription_shouldThrowException_whenMandatoryType() {
        // Given
        subscription.setTypeName("EVACUATION_ALERT"); // Type mandatory
        when(subscriptionRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(subscription));

        // When & Then
        assertThatThrownBy(() -> subscriptionService.deleteSubscription(userId, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot delete subscription for mandatory notification type");

        verify(subscriptionRepository, never()).delete(any());
    }

    @Test
    void deleteSubscription_shouldThrowException_whenSubscriptionNotFound() {
        // Given
        when(subscriptionRepository.findByIdAndUserId(999L, userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> subscriptionService.deleteSubscription(userId, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Subscription not found");

        verify(subscriptionRepository, never()).delete(any());
    }

    @Test
    void subscribe_shouldHandleAllMandatoryTypes() {
        // Test que tous les types mandatory peuvent être souscrits
        for (NotificationType type : NotificationType.values()) {
            if (type.isMandatory()) {
                SubscribeRequest mandatoryRequest = new SubscribeRequest();
                mandatoryRequest.setTypeName(type.name());
                mandatoryRequest.setActive(true);

                NotificationSubscription mandatorySub = new NotificationSubscription();
                mandatorySub.setUserId(userId);
                mandatorySub.setTypeName(type.name());
                mandatorySub.setActive(true);

                when(subscriptionRepository.save(any(NotificationSubscription.class))).thenReturn(mandatorySub);

                // When
                SubscriptionResponse result = subscriptionService.subscribe(userId, mandatoryRequest);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getTypeName()).isEqualTo(type.name());
            }
        }
    }
}
