package com.ecommerce.notificationservice.kafka;

import com.ecommerce.notificationservice.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    @Autowired private NotificationService notificationService;
    @Autowired private ObjectMapper objectMapper;

    @KafkaListener(topics = {
        KafkaTopics.ORDER_CREATED,
        KafkaTopics.INVENTORY_INSUFFICIENT,
        KafkaTopics.PAYMENT_COMPLETED,
        KafkaTopics.PAYMENT_FAILED,
        KafkaTopics.SHIPMENT_CREATED,
        KafkaTopics.SHIPMENT_DISPATCHED,
        KafkaTopics.SHIPMENT_DELIVERED
    }, groupId = "notification-service-group")
    public void onEvent(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String type = (String) event.get("eventType");
            String orderId = (String) event.get("orderId");
            String email = (String) event.getOrDefault("customerEmail", "unknown");
            log.info("Sending notification for event={} orderId={}", type, orderId);
            notificationService.send(type, orderId, email, event);
        } catch (Exception e) {
            log.error("Error processing notification event: {}", e.getMessage());
        }
    }
}
