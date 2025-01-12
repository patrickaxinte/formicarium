package com.example.formicarium.controller;

import com.example.formicarium.entity.Notification;
import com.example.formicarium.entity.User;
import com.example.formicarium.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // creeaza manual o notificare (optional)
    // exemplu: POST /api/notifications cu json: { "recipientId":5, "message":"Hello" }
    @PostMapping
    public Notification create(@RequestBody Notification notification) {
        return notificationService.createAndSendNotification(notification);
    }

    // obtine toate notificarile pentru utilizatorul curent, paginat
    @GetMapping
    public Page<Notification> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        return notificationService.getNotificationsForUser(currentUser.getId(), page, size);
    }

    // obtine notificarile necitite pentru utilizatorul curent, paginat
    @GetMapping("/unread")
    public Page<Notification> getUnreadNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        return notificationService.getUnreadNotifications(currentUser.getId(), page, size);
    }

    // marcheaza o notificare ca citita
    @PostMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        notificationService.markAsRead(id, currentUser.getId());
    }

    // sterge o singura notificare
    @DeleteMapping("/{id}")
    public void deleteOne(@PathVariable Long id, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        notificationService.deleteNotification(id, currentUser.getId());
    }

    // sterge toate notificarile pentru utilizatorul curent
    @DeleteMapping("/clearAll")
    public void clearAll(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        notificationService.deleteAllForUser(currentUser.getId());
    }
}
