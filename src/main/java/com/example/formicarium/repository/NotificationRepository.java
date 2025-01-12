package com.example.formicarium.repository;

import com.example.formicarium.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // toate notificarile pentru un utilizator, cele mai noi primele
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    // doar notificarile necitite, cele mai noi primele
    Page<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId, Pageable pageable);
    // pentru stergerea in bloc dupa id-ul utilizatorului
    void deleteByRecipientId(Long recipientId);
}
