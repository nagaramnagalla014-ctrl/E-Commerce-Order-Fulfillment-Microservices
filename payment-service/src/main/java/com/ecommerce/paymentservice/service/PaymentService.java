package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.exception.PaymentException;
import com.ecommerce.paymentservice.kafka.KafkaTopics;
import com.ecommerce.paymentservice.kafka.PaymentEventProducer;
import com.ecommerce.paymentservice.model.Payment;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Autowired private PaymentRepository paymentRepository;
    @Autowired private PaymentEventProducer producer;

    @Value("${payment.approval-rate:0.95}")
    private double approvalRate;

    @Transactional
    public Payment processPayment(String orderId, String customerId, String customerEmail,
                                  BigDecimal amount, String paymentMethod) {
        String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setOrderId(orderId);
        payment.setCustomerId(customerId);
        payment.setCustomerEmail(customerEmail);
        payment.setAmount(amount);
        payment.setCurrency("USD");
        payment.setMethod(Payment.PaymentMethod.valueOf(
                paymentMethod.toUpperCase().replace(" ", "_")));
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);

        boolean approved = Math.random() < approvalRate;

        if (approved) {
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setProcessedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "PAYMENT_COMPLETED");
            event.put("orderId", orderId);
            event.put("paymentId", paymentId);
            event.put("customerId", customerId);
            event.put("customerEmail", customerEmail);
            event.put("amount", amount);
            event.put("shippingAddress", null);
            producer.publish(KafkaTopics.PAYMENT_COMPLETED, event);
            log.info("Payment completed for order {}", orderId);
        } else {
            String reason = "Payment declined by card issuer";
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason(reason);
            paymentRepository.save(payment);

            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "PAYMENT_FAILED");
            event.put("orderId", orderId);
            event.put("reason", reason);
            producer.publish(KafkaTopics.PAYMENT_FAILED, event);
            log.warn("Payment failed for order {}", orderId);
        }

        return payment;
    }

    public Payment getByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentException("Payment not found for order: " + orderId));
    }

    public List<Payment> getAll() { return paymentRepository.findAll(); }
    public List<Payment> getByCustomer(String customerId) {
        return paymentRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }
}
