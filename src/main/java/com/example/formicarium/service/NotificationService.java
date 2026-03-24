package com.example.formicarium.service;

import com.example.formicarium.entity.Notification;
import com.example.formicarium.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    // logger for recording messages and errors
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository,
                               SimpMessagingTemplate simpMessagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    // method to create and send a notification
    @Transactional
    public Notification createAndSendNotification(Notification notification) {
        // save the notification in the database
        Notification saved = notificationRepository.save(notification);

        // define the destination for sending the notification
        String destination = "/topic/notifications/" + saved.getRecipientId();
        log.info("Trimitere notificare catre: {} cu mesajul: {}", destination, saved.getMessage());

        try {
            // send the saved notification to the specified destination
            simpMessagingTemplate.convertAndSend(destination, saved);
            log.info("Notificarea a fost trimisa cu succes catre topicul: {}", destination);
        } catch (Exception e) {
            log.error("Eroare la trimiterea notificarii catre topicul: {}", destination, e);
        }
        return saved;
    }

    // method to get notifications for a user, paginated
    @Transactional(readOnly = true)
    public Page<Notification> getNotificationsForUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
    }

    // method to get unread notifications for a user, paginated
    @Transactional(readOnly = true)
    public Page<Notification> getUnreadNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable);
    }

    // method to mark a notification as read
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        // find notification by id or throw exception
        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notificarea nu a fost gasita"));
        // check if the notification belongs to the user
        if (!notif.getRecipientId().equals(userId)) {
            throw new SecurityException("Notificarea nu apartine utilizatorului");
        }
        // mark the notification as read and save
        notif.setRead(true);
        notificationRepository.save(notif);
    }

    // method to delete a notification
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        // find notification by id or throw an exception
        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notificarea nu a fost gasita"));
        // check if the notification belongs to the user
        if (!notif.getRecipientId().equals(userId)) {
            throw new SecurityException("Notificarea nu apartine utilizatorului");
        }
        // delete the notification
        notificationRepository.delete(notif);
    }

    // method to delete all notifications for a user
    @Transactional
    public void deleteAllForUser(Long userId) {
        notificationRepository.deleteByRecipientId(userId);
    }
}
