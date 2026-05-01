package com.example.demo.dto;

import lombok.Data;

@Data // Lombok generates getters and setters
public class LoginRequest {
    private String email;
    private String password;
}