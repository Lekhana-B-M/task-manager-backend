package com.example.demo.repository;

import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Find all notifications for a user, newest first
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    // Find only unread notifications for a user
    List<Notification> findByUserAndReadStatusFalseOrderByCreatedAtDesc(User user);
}