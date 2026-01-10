package com.moveit.notification.service.impl;

import com.moveit.notification.dto.NotificationCreateDTO;
import com.moveit.notification.dto.NotificationUpdateDTO;
import com.moveit.notification.entity.Notification;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.repository.NotificationRepository;
import com.moveit.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public Page<Notification> getNotifications(NotificationType type, Long incidentId, Long eventId, Pageable pageable) {
        // Si aucun filtre, retourner toutes les notifications paginées
        if (type == null && incidentId == null && eventId == null) {
            return notificationRepository.findAll(pageable);
        }

        // Si filtre par type uniquement
        if (type != null && incidentId == null && eventId == null) {
            return notificationRepository.findByNotificationType(type, pageable);
        }

        // Si filtre par incident uniquement
        if (incidentId != null && type == null && eventId == null) {
            return notificationRepository.findByIncidentIdsContaining(incidentId, pageable);
        }

        // Si filtre par event uniquement
        if (eventId != null && type == null && incidentId == null) {
            return notificationRepository.findByEventIdsContaining(eventId, pageable);
        }

        // Combinaisons multiples : on filtre manuellement puis on page
        List<Notification> results = notificationRepository.findAll();

        if (type != null) {
            results = results.stream()
                    .filter(n -> n.getNotificationType() == type)
                    .toList();
        }

        if (incidentId != null) {
            results = results.stream()
                    .filter(n -> n.getIncidentIds().contains(incidentId))
                    .toList();
        }

        if (eventId != null) {
            results = results.stream()
                    .filter(n -> n.getEventIds().contains(eventId))
                    .toList();
        }

        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), results.size());
        List<Notification> pageContent = results.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, results.size());
    }

    @Override
    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    @Override
    public Notification createNotification(NotificationCreateDTO dto) {
        Notification notification = new Notification();
        notification.setTitle(dto.getTitle());
        notification.setContent(dto.getContent());
        notification.setNotificationType(dto.getNotificationType());
        notification.setIncidentIds(dto.getIncidentIds());
        notification.setEventIds(dto.getEventIds());
        return notificationRepository.save(notification);
    }

    @Override
    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new jakarta.persistence.EntityNotFoundException("Notification non trouvée avec l'id: " + id);
        }
        notificationRepository.deleteById(id);
    }

    @Override
    public Optional<Notification> updateNotification(Long id, NotificationUpdateDTO dto) {
        return notificationRepository.findById(id)
                .map(notification -> {
                    if (dto.getTitle() != null) {
                        notification.setTitle(dto.getTitle());
                    }
                    if (dto.getContent() != null) {
                        notification.setContent(dto.getContent());
                    }
                    if (dto.getIncidentIds() != null) {
                        notification.setIncidentIds(dto.getIncidentIds());
                    }
                    if (dto.getEventIds() != null) {
                        notification.setEventIds(dto.getEventIds());
                    }
                    return notificationRepository.save(notification);
                });
    }
}
