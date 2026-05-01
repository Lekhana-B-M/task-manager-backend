package com.example.demo.dto;

import com.example.demo.entity.TaskPriority;
import lombok.Data;

@Data
public class UpdateTaskRequest {
    private String title;
    private String description;
    private TaskPriority priority;
}