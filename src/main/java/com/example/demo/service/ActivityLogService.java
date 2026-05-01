package com.example.demo.service;

import com.example.demo.entity.ActivityLog;
import com.example.demo.entity.User;
import com.example.demo.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    /**
     * Centralized method to record any activity in the system.
     */
    public void logActivity(User user, String action, String entityType, Long entityId, String details) {
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        
        activityLogRepository.save(log);
    }
}