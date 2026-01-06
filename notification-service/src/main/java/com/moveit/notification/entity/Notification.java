package com.moveit.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    @ElementCollection
    @CollectionTable(name = "notification_incidents", joinColumns = @JoinColumn(name = "notification_id"))
    @Column(name = "incident_id")
    private Set<Long> incidentIds = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "notification_events", joinColumns = @JoinColumn(name = "notification_id"))
    @Column(name = "event_id")
    private Set<Long> eventIds = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
