package com.notification.listener;

import com.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final EmailService emailService;

    @RabbitListener(queues = "notification.email.queue")
    public void handleNotification(Map<String, String> notificationData) {
        log.info("========================================");
        log.info("📬 NOTIFICATION RECEIVED!");
        log.info("========================================");
        log.info("Notification Data: {}", notificationData);

        try {
            String to = notificationData.get("to");
            String userId = notificationData.get("userId");
            String subject = notificationData.get("subject");
            String body = notificationData.get("body");

            log.info("To: {}", to);
            log.info("UserId: {}", userId);
            log.info("Subject: {}", subject);

            if (to != null && !to.isEmpty()) {
                log.info("🔄 Sending email directly to: {}", to);
                emailService.sendEmail(to, subject, body);
            } else if (userId != null && !userId.isEmpty()) {
                log.info("🔄 Fetching email for userId: {}", userId);
                emailService.sendEmailByUserId(Long.parseLong(userId), subject, body);
            } else {
                log.error("❌ Neither 'to' nor 'userId' provided in notification data");
            }
        } catch (Exception e) {
            log.error("❌ Error processing notification: {}", e.getMessage());
            e.printStackTrace();
        }

        log.info("========================================");
    }
}