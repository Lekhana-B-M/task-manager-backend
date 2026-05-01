package com.example.demo.dto;

import com.example.demo.entity.TaskStatus;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    private TaskStatus newStatus;
}