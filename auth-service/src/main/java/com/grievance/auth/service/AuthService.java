package com.grievance.auth.service;

import com.grievance.auth.dto.AuthResponse;
import com.grievance.auth.dto.ChangePasswordRequest;
import com.grievance.auth.dto.LoginRequest;
import com.grievance.auth.dto.RegisterRequest;
import com.grievance.auth.messaging.RabbitMQProducer;
import com.grievance.auth.model.User;
import com.grievance.auth.repository.UserRepository;
import com.grievance.common.dto.ApiResponse;
import com.grievance.common.enums.Role;
import com.grievance.common.exception.ResourceNotFoundException;
import com.grievance.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RabbitMQProducer rabbitMQProducer;

    @Transactional
    public ApiResponse<?> register(RegisterRequest request) {
        // Validate unique constraints
        if (userRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.error("Email already registered");
        }
        if (userRepository.existsByAadhaarNumber(request.getAadhaarNumber())) {
            return ApiResponse.error("Aadhaar number already registered");
        }

        // Create new user
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setAadhaarNumber(request.getAadhaarNumber());
        user.setPhone(request.getPhone());
        user.setRole(Role.CITIZEN);
        user.setIsActive(true);
        user.setIsFirstLogin(false); // Citizens don't need password change

        User savedUser = userRepository.save(user);

        // Send welcome email
        sendEmail(savedUser.getEmail(), "Welcome to Grievance Portal",
                "Dear " + savedUser.getFullName() + ",\n\nYour account has been created successfully.");

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("userId", savedUser.getId());
        responseData.put("email", savedUser.getEmail());

        return ApiResponse.success("Registration successful. Please login.", responseData);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getIsActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());
        claims.put("departmentId", user.getDepartmentId());

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole().name())
                .build();

        String token = jwtService.generateToken(userDetails, claims);

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getRole(),
                user.getIsFirstLogin(),
                "Login successful"
        );
    }

    @Transactional
    public ApiResponse<?> changePassword(ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            return ApiResponse.error("Old password is incorrect");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setIsFirstLogin(false);
        userRepository.save(user);

        sendEmail(user.getEmail(), "Password Changed",
                "Your password has been changed successfully.");

        return ApiResponse.success("Password changed successfully", null);
    }

    public ApiResponse<?> validateToken(String token) {
        try {
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            Map<String, Object> data = new HashMap<>();
            data.put("valid", true);
            data.put("userId", user.getId());
            data.put("role", user.getRole());
            data.put("email", user.getEmail());

            return ApiResponse.success("Token is valid", data);
        } catch (Exception e) {
            return ApiResponse.error("Invalid token");
        }
    }

    public ApiResponse<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("fullName", user.getFullName());
        userData.put("email", user.getEmail());
        userData.put("role", user.getRole());
        userData.put("departmentId", user.getDepartmentId());
        userData.put("phone", user.getPhone());
        userData.put("aadhaarNumber", user.getAadhaarNumber());

        return ApiResponse.success("User details fetched", userData);
    }

    private void sendEmail(String to, String subject, String body) {
        Map<String, String> emailData = new HashMap<>();
        emailData.put("to", to);
        emailData.put("subject", subject);
        emailData.put("body", body);
        rabbitMQProducer.sendNotification(emailData);
    }
}