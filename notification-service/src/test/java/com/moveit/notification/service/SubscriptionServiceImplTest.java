package com.moveit.notification.service;

import com.moveit.notification.dto.SubscriptionCreateDTO;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.entity.Subscription;
import com.moveit.notification.repository.SubscriptionRepository;
import com.moveit.notification.service.impl.SubscriptionServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetSubscriptionsWithoutFilters() {
        // Given
        Subscription sub1 = new Subscription(1L, "user1", NotificationType.INCIDENT, true);
        Subscription sub2 = new Subscription(2L, "user2", NotificationType.ALERT, true);
        List<Subscription> allSubs = List.of(sub1, sub2);
        when(subscriptionRepository.findAll()).thenReturn(allSubs);

        // When
        List<Subscription> result = subscriptionService.getSubscriptions(null, null);

        // Then
        assertThat(result).hasSize(2);
        verify(subscriptionRepository, times(1)).findAll();
    }

    @Test
    void testGetSubscriptionsByUserId() {
        // Given
        Subscription sub1 = new Subscription(1L, "user1", NotificationType.INCIDENT, true);
        Subscription sub2 = new Subscription(2L, "user1", NotificationType.ALERT, true);
        when(subscriptionRepository.findByUserId("user1")).thenReturn(List.of(sub1, sub2));

        // When
        List<Subscription> result = subscriptionService.getSubscriptions("user1", null);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> s.getUserId().equals("user1"));
        verify(subscriptionRepository, times(1)).findByUserId("user1");
    }

    @Test
    void testGetSubscriptionsByType() {
        // Given
        Subscription sub1 = new Subscription(1L, "user1", NotificationType.INCIDENT, true);
        Subscription sub2 = new Subscription(2L, "user2", NotificationType.INCIDENT, true);
        when(subscriptionRepository.findByNotificationType(NotificationType.INCIDENT))
            .thenReturn(List.of(sub1, sub2));

        // When
        List<Subscription> result = subscriptionService.getSubscriptions(null, NotificationType.INCIDENT);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> s.getNotificationType() == NotificationType.INCIDENT);
        verify(subscriptionRepository, times(1)).findByNotificationType(NotificationType.INCIDENT);
    }

    @Test
    void testGetSubscriptionsByUserIdAndType() {
        // Given
        Subscription sub1 = new Subscription(1L, "user1", NotificationType.INCIDENT, true);
        Subscription sub2 = new Subscription(2L, "user1", NotificationType.ALERT, true);
        when(subscriptionRepository.findByUserId("user1")).thenReturn(List.of(sub1, sub2));

        // When
        List<Subscription> result = subscriptionService.getSubscriptions("user1", NotificationType.INCIDENT);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo("user1");
        assertThat(result.get(0).getNotificationType()).isEqualTo(NotificationType.INCIDENT);
        verify(subscriptionRepository, times(1)).findByUserId("user1");
    }

    @Test
    void testGetSubscriptionById() {
        // Given
        Subscription sub = new Subscription(10L, "user1", NotificationType.SYSTEM, true);
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));

        // When
        Optional<Subscription> result = subscriptionService.getSubscriptionById(10L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(10L);
        assertThat(result.get().getUserId()).isEqualTo("user1");
        verify(subscriptionRepository, times(1)).findById(10L);
    }

    @Test
    void testGetSubscriptionById_NotFound() {
        // Given
        when(subscriptionRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Subscription> result = subscriptionService.getSubscriptionById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(subscriptionRepository, times(1)).findById(999L);
    }

    @Test
    void testCreateSubscription_NewSubscription() {
        // Given
        SubscriptionCreateDTO dto = new SubscriptionCreateDTO("user1", NotificationType.INCIDENT);
        Subscription savedSub = new Subscription(1L, "user1", NotificationType.INCIDENT, true);
        
        when(subscriptionRepository.findByUserIdAndNotificationType("user1", NotificationType.INCIDENT))
            .thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(savedSub);

        // When
        Subscription result = subscriptionService.createSubscription(dto);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo("user1");
        assertThat(result.getNotificationType()).isEqualTo(NotificationType.INCIDENT);
        assertThat(result.getActive()).isTrue();
        verify(subscriptionRepository, times(1)).findByUserIdAndNotificationType("user1", NotificationType.INCIDENT);
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
    }

    @Test
    void testCreateSubscription_ReactivateExisting() {
        // Given
        SubscriptionCreateDTO dto = new SubscriptionCreateDTO("user2", NotificationType.ALERT);
        Subscription existingSub = new Subscription(5L, "user2", NotificationType.ALERT, false);
        Subscription reactivatedSub = new Subscription(5L, "user2", NotificationType.ALERT, true);
        
        when(subscriptionRepository.findByUserIdAndNotificationType("user2", NotificationType.ALERT))
            .thenReturn(Optional.of(existingSub));
        when(subscriptionRepository.save(existingSub)).thenReturn(reactivatedSub);

        // When
        Subscription result = subscriptionService.createSubscription(dto);

        // Then
        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getActive()).isTrue();
        verify(subscriptionRepository, times(1)).findByUserIdAndNotificationType("user2", NotificationType.ALERT);
        verify(subscriptionRepository, times(1)).save(existingSub);
    }

    @Test
    void testCreateSubscription_AlreadyActiveDoesNothing() {
        // Given
        SubscriptionCreateDTO dto = new SubscriptionCreateDTO("user3", NotificationType.SYSTEM);
        Subscription existingSub = new Subscription(10L, "user3", NotificationType.SYSTEM, true);
        
        when(subscriptionRepository.findByUserIdAndNotificationType("user3", NotificationType.SYSTEM))
            .thenReturn(Optional.of(existingSub));
        when(subscriptionRepository.save(existingSub)).thenReturn(existingSub);

        // When
        Subscription result = subscriptionService.createSubscription(dto);

        // Then
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getActive()).isTrue();
        verify(subscriptionRepository, times(1)).save(existingSub);
    }

    @Test
    void testToggleSubscription_ActivateToInactive() {
        // Given
        Subscription sub = new Subscription(20L, "user1", NotificationType.EVENT, true);
        when(subscriptionRepository.findById(20L)).thenReturn(Optional.of(sub));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        Optional<Subscription> result = subscriptionService.toggleSubscription(20L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getActive()).isFalse();
        verify(subscriptionRepository, times(1)).findById(20L);
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
    }

    @Test
    void testToggleSubscription_InactiveToActive() {
        // Given
        Subscription sub = new Subscription(21L, "user2", NotificationType.MAINTENANCE, false);
        when(subscriptionRepository.findById(21L)).thenReturn(Optional.of(sub));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        Optional<Subscription> result = subscriptionService.toggleSubscription(21L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getActive()).isTrue();
        verify(subscriptionRepository, times(1)).findById(21L);
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
    }

    @Test
    void testToggleSubscription_NotFound() {
        // Given
        when(subscriptionRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Subscription> result = subscriptionService.toggleSubscription(999L);

        // Then
        assertThat(result).isEmpty();
        verify(subscriptionRepository, times(1)).findById(999L);
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void testDeleteSubscription() {
        // Given
        when(subscriptionRepository.existsById(30L)).thenReturn(true);
        doNothing().when(subscriptionRepository).deleteById(30L);

        // When
        subscriptionService.deleteSubscription(30L);

        // Then
        verify(subscriptionRepository, times(1)).existsById(30L);
        verify(subscriptionRepository, times(1)).deleteById(30L);
    }

    @Test
    void testDeleteSubscription_NotFound() {
        // Given
        when(subscriptionRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> subscriptionService.deleteSubscription(999L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Subscription non trouvée avec l'id: 999");
        
        verify(subscriptionRepository, times(1)).existsById(999L);
        verify(subscriptionRepository, never()).deleteById(anyLong());
    }
}
