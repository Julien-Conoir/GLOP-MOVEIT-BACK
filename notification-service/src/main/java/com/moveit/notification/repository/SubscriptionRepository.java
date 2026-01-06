package com.moveit.notification.repository;

import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUserId(String userId);
    List<Subscription> findByNotificationType(NotificationType notificationType);
    Optional<Subscription> findByUserIdAndNotificationType(String userId, NotificationType notificationType);
}
