package com.moveit.notification.repository;

import com.moveit.notification.entity.NotificationLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationLevelRepository extends JpaRepository<NotificationLevel, Integer> {
    
    Optional<NotificationLevel> findByName(String name);
}
