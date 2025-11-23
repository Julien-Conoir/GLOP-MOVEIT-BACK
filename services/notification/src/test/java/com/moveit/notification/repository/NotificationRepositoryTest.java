package com.moveit.notification.repository;

import com.moveit.notification.entity.Notification;
import com.moveit.notification.entity.NotificationLevel;
import com.moveit.notification.entity.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationLevelRepository notificationLevelRepository;

    private NotificationLevel criticalLevel;
    private NotificationLevel organizationalLevel;
    private Long userId = 123L;

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

        // Créer des notifications de test
        createNotification(userId, NotificationType.SECURITY_INCIDENT, criticalLevel, false);
        createNotification(userId, NotificationType.ATHLETE_SUMMONS, organizationalLevel, true);
        createNotification(userId, NotificationType.EMERGENCY_ALERT, criticalLevel, false);
        createNotification(456L, NotificationType.GENERAL_INFO, organizationalLevel, false);

        entityManager.flush();
    }

    private void createNotification(Long userId, NotificationType type, NotificationLevel level, boolean read) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setLevel(level);
        notification.setName("Test notification");
        notification.setBody("Test body");
        notification.setRead(read);
        entityManager.persist(notification);
    }

    @Test
    void findByUserIdWithFilters_shouldReturnAllNotificationsForUser() {
        // When
        Page<Notification> result = notificationRepository.findByUserIdWithFilters(
                userId, null, null, PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    void findByUserIdWithFilters_shouldFilterByLevelName() {
        // When
        Page<Notification> result = notificationRepository.findByUserIdWithFilters(
                userId, "CRITIQUE", null, PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(n -> n.getLevel().getName().equals("CRITIQUE"));
    }

    @Test
    void findByUserIdWithFilters_shouldFilterByReadStatus() {
        // When
        Page<Notification> result = notificationRepository.findByUserIdWithFilters(
                userId, null, false, PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(n -> !n.getRead());
    }

    @Test
    void findByUserIdWithFilters_shouldOrderByPriorityThenDate() {
        // When
        Page<Notification> result = notificationRepository.findByUserIdWithFilters(
                userId, null, null, PageRequest.of(0, 10)
        );

        // Then
        List<Notification> notifications = result.getContent();
        assertThat(notifications).hasSize(3);
        // Les 2 premières doivent être critiques (priorité 1)
        assertThat(notifications.get(0).getLevel().getPriority()).isEqualTo(1);
        assertThat(notifications.get(1).getLevel().getPriority()).isEqualTo(1);
        // La dernière doit être organisationnelle (priorité 2)
        assertThat(notifications.get(2).getLevel().getPriority()).isEqualTo(2);
    }

    @Test
    void countByUserIdAndReadFalse_shouldReturnCorrectCount() {
        // When
        long count = notificationRepository.countByUserIdAndReadFalse(userId);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void findCriticalUnreadByUserId_shouldReturnOnlyCriticalUnread() {
        // When
        List<Notification> result = notificationRepository.findCriticalUnreadByUserId(userId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(n -> n.getLevel().getName().equals("CRITIQUE"));
        assertThat(result).allMatch(n -> !n.getRead());
    }

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
