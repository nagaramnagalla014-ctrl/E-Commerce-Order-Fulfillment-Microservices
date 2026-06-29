package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.OrderEvent;
import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.exception.OrderException;
import com.ecommerce.orderservice.kafka.KafkaTopics;
import com.ecommerce.orderservice.kafka.OrderEventProducer;
import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.model.OrderItem;
import com.ecommerce.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderEventProducer producer;

    @Transactional
    public Order placeOrder(OrderRequest req) {
        String orderId = "ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Order order = new Order();
        order.setOrderId(orderId);
        order.setCustomerId(req.getCustomerId());
        order.setCustomerEmail(req.getCustomerEmail());
        order.setCustomerName(req.getCustomerName());
        order.setShippingAddress(req.getShippingAddress());
        order.setPaymentMethod(req.getPaymentMethod());
        order.setStatus(Order.OrderStatus.PENDING);

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderRequest.OrderItemRequest itemReq : req.getItems()) {
            OrderItem item = new OrderItem();
            item.setSku(itemReq.getSku());
            item.setProductName(itemReq.getProductName());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(itemReq.getUnitPrice());
            BigDecimal subtotal = itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            item.setSubtotal(subtotal);
            item.setOrder(order);
            items.add(item);
            total = total.add(subtotal);
        }

        order.setTotalAmount(total);
        order.setItems(items);
        order = orderRepository.save(order);

        // Publish order-created event
        OrderEvent event = new OrderEvent();
        event.setEventType("ORDER_CREATED");
        event.setOrderId(orderId);
        event.setCustomerId(req.getCustomerId());
        event.setCustomerEmail(req.getCustomerEmail());
        event.setCustomerName(req.getCustomerName());
        event.setShippingAddress(req.getShippingAddress());
        event.setTotalAmount(total);
        event.setPaymentMethod(req.getPaymentMethod());
        event.setItems(items.stream().map(i -> {
            OrderEvent.ItemDto dto = new OrderEvent.ItemDto();
            dto.setSku(i.getSku());
            dto.setProductName(i.getProductName());
            dto.setQuantity(i.getQuantity());
            dto.setUnitPrice(i.getUnitPrice());
            return dto;
        }).collect(Collectors.toList()));

        producer.publish(KafkaTopics.ORDER_CREATED, event);
        return order;
    }

    public Order getOrder(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderException("Order not found: " + orderId));
    }

    public List<Order> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Order cancelOrder(String orderId) {
        Order order = getOrder(orderId);
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new OrderException("Only PENDING orders can be cancelled");
        }
        order.setStatus(Order.OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }
}
