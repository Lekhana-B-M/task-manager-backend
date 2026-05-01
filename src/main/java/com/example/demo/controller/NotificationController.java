package com.example.demo.controller;

import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.NotificationService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    // Helper: Get logged-in user
    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).get();
    }

    // 1. Get all notifications for logged-in user
    // URL: GET http://localhost:8080/api/notifications
    @GetMapping
    public List<Notification> getMyNotifications() {
        return notificationService.getNotificationsForUser(getAuthenticatedUser());
    }

    // 2. Mark a specific notification as read
    // URL: PATCH http://localhost:8080/api/notifications/{id}/read
    @PatchMapping("/{id}/read")
    public String markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id, getAuthenticatedUser());
        return "Notification marked as read!";
    }
}