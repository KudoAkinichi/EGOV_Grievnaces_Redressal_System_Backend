package com.grievance.auth.dto;

import com.grievance.common.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private Role role;
    private Boolean isFirstLogin;
    private String message;
}