package com.moveit.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Représente les 3 niveaux de notification configurables en base (US Admin).
 * - CRITIQUE (priorité 1) : incidents, urgences, alertes de sécurité
 * - ORGANISATIONNEL (priorité 2) : convocations, reports, plannings
 * - INFORMATIONNEL (priorité 3) : informations générales
 */
@Entity
@Table(name = "notification_level")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "level_id")
    private Integer id;

    @Column(name = "level_name", nullable = false, unique = true, length = 50)
    private String name; // CRITIQUE, ORGANISATIONNEL, INFORMATIONNEL

    @Column(name = "priority", nullable = false)
    private Integer priority; // 1 = plus haute priorité (Critique)
}
