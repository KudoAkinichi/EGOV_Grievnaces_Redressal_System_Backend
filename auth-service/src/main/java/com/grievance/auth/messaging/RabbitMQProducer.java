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
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, emailData);
            log.info("Email notification sent to queue: {}", emailData.get("to"));
        } catch (Exception e) {
            log.error("Failed to send email notification: {}", e.getMessage());
        }
    }
}