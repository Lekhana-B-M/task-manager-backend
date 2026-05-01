package com.example.demo.service;

import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.repository.NotificationRepository;

import java.util.List;

import org.springframework.stereotype.Service;


@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // Method to send a notification to a user
    public void sendNotification(User recipient, String message) {
        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setMessage(message);
        notification.setReadStatus(false);
        
        notificationRepository.save(notification);
    }
    
    public void createNotification(User recipient, String message) {
        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setMessage(message);
        notification.setReadStatus(false);
        notificationRepository.save(notification);
    }
    
 // Add these methods to your existing NotificationService.java

    public List<Notification> getNotificationsForUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public void markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        // SECURITY: Ensure the user owns this notification
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access Denied: You cannot read someone else's notification");
        }

        notification.setReadStatus(true);
        notificationRepository.save(notification);
    }
}