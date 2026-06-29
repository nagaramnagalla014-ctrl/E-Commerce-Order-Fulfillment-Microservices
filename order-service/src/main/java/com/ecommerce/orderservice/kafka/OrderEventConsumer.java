package com.ecommerce.orderservice.kafka;

import com.ecommerce.orderservice.dto.OrderEvent;
import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    @Autowired private OrderRepository orderRepository;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private OrderEventProducer producer;

    @KafkaListener(topics = {
        KafkaTopics.INVENTORY_INSUFFICIENT,
        KafkaTopics.PAYMENT_COMPLETED,
        KafkaTopics.PAYMENT_FAILED,
        KafkaTopics.SHIPMENT_CREATED,
        KafkaTopics.SHIPMENT_DISPATCHED,
        KafkaTopics.SHIPMENT_DELIVERED
    }, groupId = "order-service-group")
    public void consume(String message) {
        try {
            OrderEvent event = objectMapper.readValue(message, OrderEvent.class);
            String type = event.getEventType();

            orderRepository.findByOrderId(event.getOrderId()).ifPresent(order -> {
                switch (type) {
                    case "INVENTORY_INSUFFICIENT" -> {
                        order.setStatus(Order.OrderStatus.FAILED);
                        order.setFailureReason(event.getReason());
                    }
                    case "PAYMENT_COMPLETED" -> {
                        order.setStatus(Order.OrderStatus.CONFIRMED);
                        // publish order-confirmed
                        OrderEvent confirmed = buildEvent("ORDER_CONFIRMED", order);
                        confirmed.setPaymentId(event.getPaymentId());
                        producer.publish(KafkaTopics.ORDER_CONFIRMED, confirmed);
                    }
                    case "PAYMENT_FAILED" -> {
                        order.setStatus(Order.OrderStatus.FAILED);
                        order.setFailureReason("Payment failed: " + event.getReason());
                    }
                    case "SHIPMENT_CREATED" -> {
                        order.setStatus(Order.OrderStatus.PROCESSING);
                    }
                    case "SHIPMENT_DISPATCHED" -> {
                        order.setStatus(Order.OrderStatus.SHIPPED);
                    }
                    case "SHIPMENT_DELIVERED" -> {
                        order.setStatus(Order.OrderStatus.DELIVERED);
                    }
                }
                orderRepository.save(order);
                log.info("Order {} updated to {} on event {}", order.getOrderId(), order.getStatus(), type);
            });
        } catch (Exception e) {
            log.error("Failed to process event: {}", e.getMessage());
        }
    }

    private OrderEvent buildEvent(String type, Order order) {
        OrderEvent e = new OrderEvent();
        e.setEventType(type);
        e.setOrderId(order.getOrderId());
        e.setCustomerId(order.getCustomerId());
        e.setCustomerEmail(order.getCustomerEmail());
        e.setCustomerName(order.getCustomerName());
        e.setShippingAddress(order.getShippingAddress());
        e.setTotalAmount(order.getTotalAmount());
        e.setPaymentMethod(order.getPaymentMethod());
        return e;
    }
}
