package com.ecommerce.notificationservice.repository;

import com.ecommerce.notificationservice.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByOrderIdOrderByCreatedAtDesc(String orderId);
    List<Notification> findByRecipientEmailOrderByCreatedAtDesc(String email);
    List<Notification> findByTypeOrderByCreatedAtDesc(String type);
    List<Notification> findAllByOrderByCreatedAtDesc();
}
