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

    // logger pentru a inregistra mesaje si erori
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository,
                               SimpMessagingTemplate simpMessagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    // metoda pentru a crea si trimite o notificare
    @Transactional
    public Notification createAndSendNotification(Notification notification) {
        // salveaza notificarea in baza de date
        Notification saved = notificationRepository.save(notification);

        // defineste destinatia pentru trimiterea notificarii
        String destination = "/topic/notifications/" + saved.getRecipientId();
        log.info("Trimitere notificare catre: {} cu mesajul: {}", destination, saved.getMessage());

        try {
            // trimite notificarea salvata la destinatia specificata
            simpMessagingTemplate.convertAndSend(destination, saved);
            log.info("Notificarea a fost trimisa cu succes catre topicul: {}", destination);
        } catch (Exception e) {
            log.error("Eroare la trimiterea notificarii catre topicul: {}", destination, e);
        }
        return saved;
    }

    // metoda pentru a obtine notificari pentru un utilizator, paginat
    @Transactional(readOnly = true)
    public Page<Notification> getNotificationsForUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
    }

    // metoda pentru a obtine notificari necitite pentru un utilizator, paginat
    @Transactional(readOnly = true)
    public Page<Notification> getUnreadNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable);
    }

    // metoda pentru a marca o notificare ca citita
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        // gaseste notificarea dupa id sau intra in exceptie
        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notificarea nu a fost gasita"));
        // verifica daca notificarea apartine utilizatorului
        if (!notif.getRecipientId().equals(userId)) {
            throw new SecurityException("Notificarea nu apartine utilizatorului");
        }
        // marcheaza notificarea ca citita si salveaza
        notif.setRead(true);
        notificationRepository.save(notif);
    }

    // metoda pentru a sterge o notificare
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        // gaseste notificarea dupa id sau arunca o exceptie
        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notificarea nu a fost gasita"));
        // verifica daca notificarea apartine utilizatorului
        if (!notif.getRecipientId().equals(userId)) {
            throw new SecurityException("Notificarea nu apartine utilizatorului");
        }
        // sterge notificarea
        notificationRepository.delete(notif);
    }

    // metoda pentru a sterge toate notificarile unui utilizator
    @Transactional
    public void deleteAllForUser(Long userId) {
        notificationRepository.deleteByRecipientId(userId);
    }
}
