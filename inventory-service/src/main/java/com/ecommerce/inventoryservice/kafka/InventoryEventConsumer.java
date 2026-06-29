package com.ecommerce.inventoryservice.kafka;

import com.ecommerce.inventoryservice.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InventoryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);

    @Autowired private InventoryService inventoryService;
    @Autowired private ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "inventory-service-group")
    public void onOrderCreated(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String orderId = (String) event.get("orderId");
            log.info("Processing order-created for orderId={}", orderId);
            inventoryService.reserveInventory(event);
        } catch (Exception e) {
            log.error("Error processing order-created event: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = "inventory-service-group")
    public void onPaymentFailed(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String orderId = (String) event.get("orderId");
            log.info("Releasing inventory for failed payment orderId={}", orderId);
            inventoryService.releaseReservation(orderId);
        } catch (Exception e) {
            log.error("Error processing payment-failed event: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = KafkaTopics.ORDER_CONFIRMED, groupId = "inventory-service-group")
    public void onOrderConfirmed(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String orderId = (String) event.get("orderId");
            inventoryService.consumeReservation(orderId);
        } catch (Exception e) {
            log.error("Error processing order-confirmed event: {}", e.getMessage());
        }
    }
}
