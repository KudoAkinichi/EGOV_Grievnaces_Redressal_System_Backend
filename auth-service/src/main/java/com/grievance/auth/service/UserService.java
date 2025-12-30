package com.grievance.auth.service;

import com.grievance.auth.dto.CreateOfficerRequest;
import com.grievance.auth.messaging.RabbitMQProducer;
import com.grievance.auth.model.User;
import com.grievance.auth.repository.UserRepository;
import com.grievance.common.dto.ApiResponse;
import com.grievance.common.enums.Role;
import com.grievance.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RabbitMQProducer rabbitMQProducer;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$";

    public ApiResponse<Page<User>> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAllActive(pageable);
        return ApiResponse.success("Users fetched successfully", users);
    }

    public ApiResponse<User> getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return ApiResponse.success("User fetched successfully", user);
    }

    public ApiResponse<List<User>> getUsersByRole(Role role) {
        List<User> users = userRepository.findByRole(role);
        return ApiResponse.success("Users fetched successfully", users);
    }

    public ApiResponse<List<User>> getUsersByDepartment(Long departmentId) {
        List<User> users = userRepository.findByDepartmentId(departmentId);
        return ApiResponse.success("Users fetched successfully", users);
    }

    @Transactional
    public ApiResponse<?> createOfficer(CreateOfficerRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.error("Email already exists");
        }

        // Generate temporary password
        String tempPassword = generateTemporaryPassword();

        User officer = new User();
        officer.setFullName(request.getFullName());
        officer.setEmail(request.getEmail());
        officer.setPasswordHash(passwordEncoder.encode(tempPassword));
        officer.setRole(request.getRole());
        officer.setDepartmentId(request.getDepartmentId());
        officer.setPhone(request.getPhone());
        officer.setIsActive(true);
        officer.setIsFirstLogin(true); // Force password change on first login

        User savedOfficer = userRepository.save(officer);

        // Send credentials email
        String emailBody = String.format(
                "Dear %s,\n\n" +
                        "Your officer account has been created.\n\n" +
                        "Login Credentials:\n" +
                        "Email: %s\n" +
                        "Temporary Password: %s\n\n" +
                        "Please login and change your password immediately.\n\n" +
                        "Regards,\nGrievance Portal Team",
                savedOfficer.getFullName(),
                savedOfficer.getEmail(),
                tempPassword
        );

        sendEmail(savedOfficer.getEmail(), "Officer Account Created - Credentials", emailBody);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("userId", savedOfficer.getId());
        responseData.put("email", savedOfficer.getEmail());
        responseData.put("temporaryPassword", tempPassword);

        return ApiResponse.success("Officer created successfully. Credentials sent via email.", responseData);
    }

    @Transactional
    public ApiResponse<?> updateUserProfile(Long userId, Map<String, Object> updates) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (updates.containsKey("fullName")) {
            user.setFullName((String) updates.get("fullName"));
        }
        if (updates.containsKey("phone")) {
            user.setPhone((String) updates.get("phone"));
        }

        userRepository.save(user);
        return ApiResponse.success("Profile updated successfully", user);
    }

    @Transactional
    public ApiResponse<?> assignRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setRole(role);
        userRepository.save(user);

        return ApiResponse.success("Role assigned successfully", user);
    }

    @Transactional
    public ApiResponse<?> assignDepartment(Long userId, Long departmentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setDepartmentId(departmentId);
        userRepository.save(user);

        return ApiResponse.success("Department assigned successfully", user);
    }

    @Transactional
    public ApiResponse<?> deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsActive(false); // Soft delete
        userRepository.save(user);

        return ApiResponse.success("User deactivated successfully", null);
    }

    public ApiResponse<List<User>> getAvailableOfficers(Long departmentId) {
        List<User> officers = userRepository.findActiveOfficersByDepartment(Role.DEPT_OFFICER, departmentId);
        return ApiResponse.success("Officers fetched successfully", officers);
    }

    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }

    private void sendEmail(String to, String subject, String body) {
        Map<String, String> emailData = new HashMap<>();
        emailData.put("to", to);
        emailData.put("subject", subject);
        emailData.put("body", body);
        rabbitMQProducer.sendNotification(emailData);
    }
}