package com.ecommerce.shippingservice.kafka;

import com.ecommerce.shippingservice.service.ShippingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ShippingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ShippingEventConsumer.class);

    @Autowired private ShippingService shippingService;
    @Autowired private ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.PAYMENT_COMPLETED, groupId = "shipping-service-group")
    public void onPaymentCompleted(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String orderId = (String) event.get("orderId");
            String customerId = (String) event.get("customerId");
            String customerEmail = (String) event.get("customerEmail");
            String shippingAddress = (String) event.get("shippingAddress");
            log.info("Creating shipment for orderId={}", orderId);
            shippingService.createShipment(orderId, customerId, customerEmail, shippingAddress);
        } catch (Exception e) {
            log.error("Error processing payment-completed event: {}", e.getMessage());
        }
    }
}
