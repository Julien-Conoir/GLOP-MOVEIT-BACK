package com.moveit.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Notification envoyée à un utilisateur.
 * Entité simplifiée : zone et métadonnées sont récupérées depuis les services métier.
 */
@Entity
@Table(name = "notification", indexes = {
        @Index(name = "idx_notification_user", columnList = "notification_user_id"),
        @Index(name = "idx_notification_type", columnList = "notification_type"),
        @Index(name = "idx_notification_level", columnList = "level_id"),
        @Index(name = "idx_notification_read", columnList = "is_read"),
        @Index(name = "idx_notification_start_date", columnList = "notification_start_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "notification_user_id", nullable = false)
    private Long userId; // FK vers user (auth-service)

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", nullable = false)
    private NotificationLevel level; // CRITIQUE, ORGANISATIONNEL, INFORMATIONNEL

    @Column(name = "notification_security", nullable = true)
    private Integer securityId; // FK nullable vers une entité security si nécessaire

    @Column(name = "notification_competition", nullable = true)
    private Integer competitionId; // FK nullable vers une compétition/épreuve

    @Column(name = "notification_name", nullable = false, length = 255)
    private String name; // Titre court de la notification

    @Column(name = "notification_body", columnDefinition = "TEXT")
    private String body; // Contenu détaillé (actions à prendre, détails incident, etc.)

    @Column(name = "is_read", nullable = false)
    private Boolean read = false; // État de lecture

    @CreationTimestamp
    @Column(name = "notification_start_date", nullable = false, updatable = false)
    private Instant startDate; // Date de création/envoi

}
