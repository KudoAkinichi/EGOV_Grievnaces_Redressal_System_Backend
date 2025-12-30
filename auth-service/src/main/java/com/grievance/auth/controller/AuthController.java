package com.grievance.auth.controller;

import com.grievance.auth.dto.AuthResponse;
import com.grievance.auth.dto.ChangePasswordRequest;
import com.grievance.auth.dto.LoginRequest;
import com.grievance.auth.dto.RegisterRequest;
import com.grievance.auth.service.AuthService;
import com.grievance.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<?>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(authService.changePassword(request));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<?>> validateToken(@RequestParam String token)
    {
        return ResponseEntity.ok(authService.validateToken(token));
    }
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout() {
        // For stateless JWT, just return success
        // Frontend will remove token
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}
