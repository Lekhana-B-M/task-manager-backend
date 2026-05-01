package com.example.demo.controller;

import java.util.Optional;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.demo.dto.LoginRequest;
import org.springframework.web.bind.annotation.*;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import com.example.demo.dto.LoginResponse;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Controller", description = "APIs for User Registration and Login")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    // Constructor Injection (No @Operation here!)
    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "Register a new user", description = "Creates a new user and hashes the password")
    @PostMapping("/register")
    public User createUser(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @Operation(summary = "Get user by email", description = "Fetches details of a specific user (Protected by JWT)")
    @GetMapping("/{email}")
    public Optional<User> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }
    
    @Operation(summary = "User Login", description = "Verifies credentials and returns a JWT token")
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        User user = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
        String token = jwtUtil.generateToken(user.getEmail());
        return new LoginResponse(token, user.getEmail());
    }
}