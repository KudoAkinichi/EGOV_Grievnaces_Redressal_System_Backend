package com.grievance.auth.controller;


import com.grievance.auth.dto.CreateOfficerRequest;
import com.grievance.auth.model.User;
import com.grievance.auth.service.UserService;
import com.grievance.common.dto.ApiResponse;
import com.grievance.common.enums.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<User>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/by-role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByRole(@PathVariable Role role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    @GetMapping("/by-department/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(userService.getUsersByDepartment(departmentId));
    }

    @PostMapping("/officers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> createOfficer(@Valid @RequestBody CreateOfficerRequest request) {
        return ResponseEntity.ok(userService.createOfficer(request));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> updateUser(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> updates
    ) {
        return ResponseEntity.ok(userService.updateUserProfile(userId, updates));
    }

    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> assignRole(
            @PathVariable Long userId,
            @RequestParam Role role
    ) {
        return ResponseEntity.ok(userService.assignRole(userId, role));
    }

    @PutMapping("/{userId}/department")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> assignDepartment(
            @PathVariable Long userId,
            @RequestParam Long departmentId
    ) {
        return ResponseEntity.ok(userService.assignDepartment(userId, departmentId));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.deleteUser(userId));
    }

    @GetMapping("/officers/available/{departmentId}")
    public ResponseEntity<ApiResponse<List<User>>> getAvailableOfficers(
            @PathVariable Long departmentId) {
        return ResponseEntity.ok(userService.getAvailableOfficers(departmentId));
    }

}
