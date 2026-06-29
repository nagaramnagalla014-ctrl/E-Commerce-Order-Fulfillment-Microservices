package com.ecommerce.shippingservice.service;

import com.ecommerce.shippingservice.exception.ShippingException;
import com.ecommerce.shippingservice.kafka.KafkaTopics;
import com.ecommerce.shippingservice.kafka.ShippingEventProducer;
import com.ecommerce.shippingservice.model.Shipment;
import com.ecommerce.shippingservice.repository.ShipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ShippingService {

    private static final Logger log = LoggerFactory.getLogger(ShippingService.class);

    private static final String[] CARRIERS = {"UPS", "FedEx", "DHL", "USPS"};

    @Autowired private ShipmentRepository shipmentRepository;
    @Autowired private ShippingEventProducer producer;

    @Transactional
    public Shipment createShipment(String orderId, String customerId, String customerEmail, String shippingAddress) {
        String shipmentId = "SHIP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String trackingNumber = "TRK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        String carrier = CARRIERS[(int) (Math.random() * CARRIERS.length)];

        Shipment shipment = new Shipment();
        shipment.setShipmentId(shipmentId);
        shipment.setOrderId(orderId);
        shipment.setCustomerId(customerId);
        shipment.setCustomerEmail(customerEmail);
        shipment.setTrackingNumber(trackingNumber);
        shipment.setCarrier(carrier);
        shipment.setShippingAddress(shippingAddress);
        shipment.setStatus(Shipment.ShipmentStatus.CREATED);
        shipment.setEstimatedDelivery(LocalDate.now().plusDays(3 + (int)(Math.random() * 4)));
        shipment = shipmentRepository.save(shipment);

        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "SHIPMENT_CREATED");
        event.put("orderId", orderId);
        event.put("shipmentId", shipmentId);
        event.put("trackingNumber", trackingNumber);
        event.put("carrier", carrier);
        event.put("customerEmail", customerEmail);
        event.put("estimatedDelivery", shipment.getEstimatedDelivery().toString());
        producer.publish(KafkaTopics.SHIPMENT_CREATED, event);
        log.info("Shipment {} created for order {}", shipmentId, orderId);
        return shipment;
    }

    @Transactional
    public Shipment dispatch(String shipmentId) {
        Shipment shipment = shipmentRepository.findByShipmentId(shipmentId)
                .orElseThrow(() -> new ShippingException("Shipment not found: " + shipmentId));
        shipment.setStatus(Shipment.ShipmentStatus.DISPATCHED);
        shipment.setDispatchedAt(LocalDateTime.now());
        shipment = shipmentRepository.save(shipment);

        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "SHIPMENT_DISPATCHED");
        event.put("orderId", shipment.getOrderId());
        event.put("shipmentId", shipmentId);
        event.put("trackingNumber", shipment.getTrackingNumber());
        event.put("carrier", shipment.getCarrier());
        event.put("customerEmail", shipment.getCustomerEmail());
        producer.publish(KafkaTopics.SHIPMENT_DISPATCHED, event);
        return shipment;
    }

    @Transactional
    public Shipment deliver(String shipmentId) {
        Shipment shipment = shipmentRepository.findByShipmentId(shipmentId)
                .orElseThrow(() -> new ShippingException("Shipment not found: " + shipmentId));
        shipment.setStatus(Shipment.ShipmentStatus.DELIVERED);
        shipment.setDeliveredAt(LocalDateTime.now());
        shipment = shipmentRepository.save(shipment);

        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "SHIPMENT_DELIVERED");
        event.put("orderId", shipment.getOrderId());
        event.put("shipmentId", shipmentId);
        event.put("customerEmail", shipment.getCustomerEmail());
        event.put("deliveredAt", shipment.getDeliveredAt().toString());
        producer.publish(KafkaTopics.SHIPMENT_DELIVERED, event);
        return shipment;
    }

    public Shipment getByOrderId(String orderId) {
        return shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ShippingException("Shipment not found for order: " + orderId));
    }

    public Shipment getByTracking(String tracking) {
        return shipmentRepository.findByTrackingNumber(tracking)
                .orElseThrow(() -> new ShippingException("Shipment not found: " + tracking));
    }

    public List<Shipment> getAll() { return shipmentRepository.findAll(); }
}
