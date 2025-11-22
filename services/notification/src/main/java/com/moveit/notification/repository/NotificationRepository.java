package com.moveit.notification.repository;

import com.moveit.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Récupère les notifications d'un utilisateur (avec filtres optionnels).
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId " +
            "AND (:levelName IS NULL OR n.level.name = :levelName) " +
            "AND (:read IS NULL OR n.read = :read) " +
            "ORDER BY n.level.priority ASC, n.startDate DESC")
    Page<Notification> findByUserIdWithFilters(
            @Param("userId") Long userId,
            @Param("levelName") String levelName,
            @Param("read") Boolean read,
            Pageable pageable
    );

    /**
     * Récupère les notifications non lues d'un utilisateur pour affichage badge.
     */
    long countByUserIdAndReadFalse(Long userId);

    /**
     * Récupère les notifications critiques non lues d'un utilisateur.
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId " +
            "AND n.level.name = 'CRITIQUE' AND n.read = false " +
            "ORDER BY n.startDate DESC")
    List<Notification> findCriticalUnreadByUserId(@Param("userId") Long userId);
}
