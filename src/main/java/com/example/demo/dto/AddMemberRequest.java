package com.example.demo.dto;

import com.example.demo.entity.BoardRole;
import lombok.Data;

@Data
public class AddMemberRequest {
    private String email;
    private BoardRole role;
}