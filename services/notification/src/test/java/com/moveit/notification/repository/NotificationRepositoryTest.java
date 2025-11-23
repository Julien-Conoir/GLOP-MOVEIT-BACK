package com.moveit.notification.repository;

import com.moveit.notification.entity.Notification;
import com.moveit.notification.entity.NotificationLevel;
import com.moveit.notification.entity.NotificationRecipient;
import com.moveit.notification.entity.NotificationSubscription;
import com.moveit.notification.entity.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests pour les repositories du nouveau modèle notification+recipient.
 */
@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationRecipientRepository notificationRecipientRepository;

    @Autowired
    private NotificationSubscriptionRepository notificationSubscriptionRepository;

    @Autowired
    private NotificationLevelRepository notificationLevelRepository;

    private NotificationLevel criticalLevel;
    private NotificationLevel organizationalLevel;
    private Long userId = 123L;
    private Notification notification1;
    private Notification notification2;
    private Notification notification3;

    @BeforeEach
    void setUp() {
        // Créer les niveaux
        criticalLevel = new NotificationLevel();
        criticalLevel.setName("CRITIQUE");
        criticalLevel.setPriority(1);
        entityManager.persist(criticalLevel);

        organizationalLevel = new NotificationLevel();
        organizationalLevel.setName("ORGANISATIONNEL");
        organizationalLevel.setPriority(2);
        entityManager.persist(organizationalLevel);

        // Créer des notifications (messages sans userId)
        notification1 = createNotificationMessage(NotificationType.SECURITY_INCIDENT, criticalLevel);
        notification2 = createNotificationMessage(NotificationType.ATHLETE_SUMMONS, organizationalLevel);
        notification3 = createNotificationMessage(NotificationType.EMERGENCY_ALERT, criticalLevel);
        Notification notification4 = createNotificationMessage(NotificationType.GENERAL_INFO, organizationalLevel);

        // Créer des recipients (qui reçoivent les notifications)
        createRecipient(notification1, userId, false);
        createRecipient(notification2, userId, true);
        createRecipient(notification3, userId, false);
        createRecipient(notification4, 456L, false); // Autre user

        entityManager.flush();
    }

    private Notification createNotificationMessage(NotificationType type, NotificationLevel level) {
        Notification notification = Notification.builder()
                .type(type)
                .level(level)
                .name("Test notification")
                .body("Test body")
                .build();
        return entityManager.persist(notification);
    }

    private NotificationRecipient createRecipient(Notification notification, Long userId, boolean read) {
        NotificationRecipient recipient = new NotificationRecipient();
        recipient.setNotification(notification);
        recipient.setUserId(userId);
        recipient.setRead(read);
        return entityManager.persist(recipient);
    }

    // ========== Tests NotificationRecipientRepository ==========

    @Test
    void findByUserIdWithFilters_shouldReturnAllRecipientsForUser() {
        // When
        Page<NotificationRecipient> result = notificationRecipientRepository.findByUserIdWithFilters(
                userId, null, null, null, PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    void findByUserIdWithFilters_shouldFilterByLevelName() {
        // When
        Page<NotificationRecipient> result = notificationRecipientRepository.findByUserIdWithFilters(
                userId, "CRITIQUE", null, null, PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .allMatch(r -> r.getNotification().getLevel().getName().equals("CRITIQUE"));
    }

    @Test
    void findByUserIdWithFilters_shouldFilterByReadStatus() {
        // When
        Page<NotificationRecipient> result = notificationRecipientRepository.findByUserIdWithFilters(
                userId, null, false, null, PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(r -> !r.getRead());
    }

    @Test
    void findByUserIdWithFilters_shouldOrderByPriorityThenDate() {
        // When
        Page<NotificationRecipient> result = notificationRecipientRepository.findByUserIdWithFilters(
                userId, null, null, null, PageRequest.of(0, 10)
        );

        // Then
        List<NotificationRecipient> recipients = result.getContent();
        assertThat(recipients).hasSize(3);
        // Les 2 premiers doivent être critiques (priorité 1)
        assertThat(recipients.get(0).getNotification().getLevel().getPriority()).isEqualTo(1);
        assertThat(recipients.get(1).getNotification().getLevel().getPriority()).isEqualTo(1);
        // Le dernier doit être organisationnel (priorité 2)
        assertThat(recipients.get(2).getNotification().getLevel().getPriority()).isEqualTo(2);
    }

    @Test
    void findByUserIdWithFilters_shouldSupportKeysetPagination() {
        // Given - Récupérer le premier résultat
        Page<NotificationRecipient> firstPage = notificationRecipientRepository.findByUserIdWithFilters(
                userId, null, null, null, PageRequest.of(0, 1)
        );
        Long firstId = firstPage.getContent().get(0).getId();

        // When - Utiliser l'afterId pour la suite
        Page<NotificationRecipient> secondPage = notificationRecipientRepository.findByUserIdWithFilters(
                userId, null, null, firstId, PageRequest.of(0, 10)
        );

        // Then - Ne doit pas contenir le premier élément
        assertThat(secondPage.getContent()).hasSize(2);
        assertThat(secondPage.getContent()).noneMatch(r -> r.getId().equals(firstId));
    }

    @Test
    void countByUserIdAndReadFalse_shouldReturnCorrectCount() {
        // When
        long count = notificationRecipientRepository.countByUserIdAndReadFalse(userId);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void findCriticalUnreadByUserId_shouldReturnOnlyCriticalUnread() {
        // When
        List<NotificationRecipient> result = notificationRecipientRepository.findCriticalUnreadByUserId(userId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .allMatch(r -> r.getNotification().getLevel().getName().equals("CRITIQUE"));
        assertThat(result).allMatch(r -> !r.getRead());
    }

    @Test
    void findByIdAndUserId_shouldReturnRecipient_whenOwnershipMatches() {
        // Given
        NotificationRecipient recipient = createRecipient(notification1, 999L, false);
        entityManager.flush();

        // When
        var result = notificationRecipientRepository.findByIdAndUserId(recipient.getId(), 999L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(999L);
    }

    @Test
    void findByIdAndUserId_shouldReturnEmpty_whenOwnershipDoesNotMatch() {
        // Given
        NotificationRecipient recipient = createRecipient(notification1, 999L, false);
        entityManager.flush();

        // When
        var result = notificationRecipientRepository.findByIdAndUserId(recipient.getId(), 888L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void markAllAsReadByUserId_shouldUpdateAllUnreadRecipients() {
        // When
        int updatedCount = notificationRecipientRepository.markAllAsReadByUserId(userId);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(updatedCount).isEqualTo(2); // 2 non lues
        long unreadCount = notificationRecipientRepository.countByUserIdAndReadFalse(userId);
        assertThat(unreadCount).isEqualTo(0);
    }

    // ========== Tests NotificationSubscriptionRepository ==========

    @Test
    void findSubscribedUserIds_shouldReturnAllSubscribedUsers() {
        // Given
        createSubscription(100L, "SECURITY_INCIDENT", "CRITIQUE", null, true);
        createSubscription(200L, null, "CRITIQUE", null, true); // Abonné à tout CRITIQUE
        createSubscription(300L, "SECURITY_INCIDENT", null, null, true); // Abonné à tout SECURITY_INCIDENT
        createSubscription(400L, "OTHER_TYPE", "CRITIQUE", null, true); // Type différent
        createSubscription(500L, "SECURITY_INCIDENT", "CRITIQUE", null, false); // Inactif
        entityManager.flush();

        // When
        Set<Long> result = notificationSubscriptionRepository.findSubscribedUserIds(
                "SECURITY_INCIDENT", "CRITIQUE", null
        );

        // Then
        assertThat(result).containsExactlyInAnyOrder(100L, 200L, 300L);
    }

    @Test
    void findSubscribedUserIds_shouldMatchBroadSubscriptions() {
        // Given
        createSubscription(100L, null, null, null, true); // Abonné à tout
        createSubscription(200L, "SECURITY_INCIDENT", null, null, true); // Type spécifique
        entityManager.flush();

        // When
        Set<Long> result = notificationSubscriptionRepository.findSubscribedUserIds(
                "SECURITY_INCIDENT", "CRITIQUE", "TOPIC_A"
        );

        // Then - Les deux doivent matcher (NULL = wildcard)
        assertThat(result).containsExactlyInAnyOrder(100L, 200L);
    }

    @Test
    void findByUserIdAndActiveTrue_shouldReturnOnlyActiveSubscriptions() {
        // Given
        createSubscription(userId, "TYPE_A", null, null, true);
        createSubscription(userId, "TYPE_B", null, null, false);
        entityManager.flush();

        // When
        List<NotificationSubscription> result = notificationSubscriptionRepository
                .findByUserIdAndActiveTrue(userId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTypeName()).isEqualTo("TYPE_A");
    }

    @Test
    void findByIdAndUserId_shouldVerifyOwnership() {
        // Given
        NotificationSubscription sub = createSubscription(userId, "TYPE_A", null, null, true);
        entityManager.flush();

        // When
        var result = notificationSubscriptionRepository.findByIdAndUserId(sub.getId(), userId);

        // Then
        assertThat(result).isPresent();
    }

    @Test
    void existsByUserIdAndActiveTrue_shouldReturnTrue_whenActiveSubscriptionExists() {
        // Given
        createSubscription(userId, "TYPE_A", null, null, true);
        entityManager.flush();

        // When
        boolean exists = notificationSubscriptionRepository.existsByUserIdAndActiveTrue(userId);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUserIdAndActiveTrue_shouldReturnFalse_whenNoActiveSubscription() {
        // When
        boolean exists = notificationSubscriptionRepository.existsByUserIdAndActiveTrue(999L);

        // Then
        assertThat(exists).isFalse();
    }

    private NotificationSubscription createSubscription(Long userId, String typeName, String levelName, 
                                                        String topic, boolean active) {
        NotificationSubscription sub = new NotificationSubscription();
        sub.setUserId(userId);
        sub.setTypeName(typeName);
        sub.setLevelName(levelName);
        sub.setTopic(topic);
        sub.setActive(active);
        return entityManager.persist(sub);
    }

    // ========== Tests NotificationLevelRepository ==========

    @Test
    void findByName_shouldReturnLevel() {
        // When
        var level = notificationLevelRepository.findByName("CRITIQUE");

        // Then
        assertThat(level).isPresent();
        assertThat(level.get().getName()).isEqualTo("CRITIQUE");
        assertThat(level.get().getPriority()).isEqualTo(1);
    }
}
