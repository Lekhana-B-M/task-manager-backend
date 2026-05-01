package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user who performed the action
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // The action performed (e.g., "CREATED", "UPDATED", "DELETED", "ASSIGNED")
    @Column(nullable = false)
    private String action;

    // The type of object affected (e.g., "TASK", "BOARD")
    @Column(nullable = false)
    private String entityType;

    // The ID of the affected object (e.g., Task ID 5)
    @Column(nullable = false)
    private Long entityId;
    
    // Optional: A small detail message (e.g., "Moved status to DONE")
    private String details;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime timestamp;
}