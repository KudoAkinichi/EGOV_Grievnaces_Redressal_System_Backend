package com.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final WebClient.Builder webClientBuilder;

    public void sendEmail(String to, String subject, String body) {
        log.info("========================================");
        log.info("📧 ATTEMPTING TO SEND EMAIL");
        log.info("========================================");
        log.info("To: {}", to);
        log.info("Subject: {}", subject);
        log.info("Body: {}", body);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@grievance-portal.gov.in");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            log.info("🔄 Sending email via JavaMailSender...");
            mailSender.send(message);

            log.info("✅✅✅ EMAIL SENT SUCCESSFULLY to: {}", to);
            log.info("========================================");
        } catch (Exception e) {
            log.error("❌❌❌ FAILED TO SEND EMAIL to: {}", to);
            log.error("Error Type: {}", e.getClass().getName());
            log.error("Error Message: {}", e.getMessage());
            log.error("========================================");
            e.printStackTrace();
        }
    }

    public void sendEmailByUserId(Long userId, String subject, String body) {
        try {
            log.info("🔍 Fetching email for user ID: {}", userId);

            Map<String, Object> response = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8081/users/" + userId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.get("data") != null) {
                Map<String, Object> userData = (Map<String, Object>) response.get("data");
                String email = (String) userData.get("email");

                log.info("✅ Fetched email: {} for user ID: {}", email, userId);
                sendEmail(email, subject, body);
            } else {
                log.error("❌ User not found with ID: {}", userId);
            }
        } catch (Exception e) {
            log.error("❌ Failed to fetch user email for userId {}: {}", userId, e.getMessage());
            e.printStackTrace();
        }
    }
}