package com.ecommerce.paymentservice.repository;

import com.ecommerce.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(String orderId);
    Optional<Payment> findByPaymentId(String paymentId);
    List<Payment> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    List<Payment> findByStatusOrderByCreatedAtDesc(Payment.PaymentStatus status);
}
