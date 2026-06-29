package com.ecommerce.notificationservice.service;

import com.ecommerce.notificationservice.model.Notification;
import com.ecommerce.notificationservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired private NotificationRepository notificationRepository;

    public void send(String eventType, String orderId, String recipientEmail, Map<String, Object> event) {
        String subject;
        String message;

        switch (eventType) {
            case "ORDER_CREATED" -> {
                subject = "Order Placed — " + orderId;
                message = "Your order " + orderId + " has been placed successfully and is being processed.";
            }
            case "INVENTORY_INSUFFICIENT" -> {
                subject = "Order Failed — " + orderId;
                message = "Unfortunately, your order " + orderId + " could not be fulfilled due to insufficient stock. " +
                        event.getOrDefault("reason", "");
            }
            case "PAYMENT_COMPLETED" -> {
                subject = "Payment Confirmed — " + orderId;
                message = "Payment for order " + orderId + " has been confirmed. Your order is being packed.";
            }
            case "PAYMENT_FAILED" -> {
                subject = "Payment Failed — " + orderId;
                message = "Payment for order " + orderId + " could not be processed. Please try again.";
            }
            case "SHIPMENT_CREATED" -> {
                subject = "Order Shipped — " + orderId;
                message = "Your order " + orderId + " has been shipped. Tracking: " +
                        event.getOrDefault("trackingNumber", "N/A") + " via " +
                        event.getOrDefault("carrier", "carrier") +
                        ". Estimated delivery: " + event.getOrDefault("estimatedDelivery", "TBD");
            }
            case "SHIPMENT_DISPATCHED" -> {
                subject = "Package Dispatched — " + orderId;
                message = "Your package for order " + orderId + " has been dispatched and is on its way!";
            }
            case "SHIPMENT_DELIVERED" -> {
                subject = "Package Delivered — " + orderId;
                message = "Your order " + orderId + " has been delivered. Enjoy your purchase!";
            }
            default -> {
                log.warn("Unknown event type: {}", eventType);
                return;
            }
        }

        Notification notification = new Notification();
        notification.setOrderId(orderId);
        notification.setRecipientEmail(recipientEmail);
        notification.setType(eventType);
        notification.setSubject(subject);
        notification.setMessage(message);
        notification.setStatus(Notification.NotificationStatus.SENT);
        notificationRepository.save(notification);

        // In production this would call an email/SMS provider like SendGrid or Twilio
        log.info("[EMAIL] To: {} | Subject: {} | Message: {}", recipientEmail, subject, message);
    }

    public List<Notification> getAll() { return notificationRepository.findAllByOrderByCreatedAtDesc(); }
    public List<Notification> getByOrderId(String orderId) { return notificationRepository.findByOrderIdOrderByCreatedAtDesc(orderId); }
}
