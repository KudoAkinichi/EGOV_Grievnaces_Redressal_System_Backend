package com.grievance.auth.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE = "grievance.exchange";
    private static final String ROUTING_KEY = "notification.email";

    public void sendNotification(Map<String, String> emailData) {
        try {
            log.info("🔄 Attempting to send notification to RabbitMQ...");
            log.info("📧 Email Data: {}", emailData);

            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, emailData);

            log.info("✅ Email notification sent to queue successfully");
            log.info("📬 Exchange: {}, Routing Key: {}", EXCHANGE, ROUTING_KEY);
        } catch (Exception e) {
            log.error("❌ Failed to send email notification: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}