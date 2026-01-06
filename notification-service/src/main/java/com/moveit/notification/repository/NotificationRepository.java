package com.moveit.notification.repository;

import com.moveit.notification.entity.Notification;
import com.moveit.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByNotificationType(NotificationType notificationType);
    List<Notification> findByIncidentIdsContaining(Long incidentId);
    List<Notification> findByEventIdsContaining(Long eventId);
}
