package com.grievance.messaging;

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

    public void sendNotification(Map<String, String> notificationData) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, notificationData);
            log.info("Notification sent to queue for user: {}", notificationData.get("userId"));
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
        }
    }
}