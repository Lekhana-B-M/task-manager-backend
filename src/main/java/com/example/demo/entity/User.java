package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users") // "user" is a reserved word in PostgreSQL, so we name the table "users"
@Data                  // Lombok: Automatically creates Getters, Setters, toString
@NoArgsConstructor     // Lombok: Creates a constructor with no arguments
@AllArgsConstructor    // Lombok: Creates a constructor with all arguments
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING) // Saves role as "USER" or "ADMIN" in DB (not 0 or 1)
    private Role role;
}