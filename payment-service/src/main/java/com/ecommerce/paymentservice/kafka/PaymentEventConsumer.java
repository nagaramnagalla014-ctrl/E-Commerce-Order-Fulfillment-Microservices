package com.ecommerce.paymentservice.kafka;

import com.ecommerce.paymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    @Autowired private PaymentService paymentService;
    @Autowired private ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.INVENTORY_RESERVED, groupId = "payment-service-group")
    public void onInventoryReserved(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String orderId = (String) event.get("orderId");
            String customerId = (String) event.get("customerId");
            String customerEmail = (String) event.get("customerEmail");
            String paymentMethod = (String) event.get("paymentMethod");
            BigDecimal amount = new BigDecimal(event.get("totalAmount").toString());
            log.info("Processing payment for orderId={}", orderId);
            paymentService.processPayment(orderId, customerId, customerEmail, amount, paymentMethod);
        } catch (Exception e) {
            log.error("Error processing inventory-reserved event: {}", e.getMessage());
        }
    }
}
