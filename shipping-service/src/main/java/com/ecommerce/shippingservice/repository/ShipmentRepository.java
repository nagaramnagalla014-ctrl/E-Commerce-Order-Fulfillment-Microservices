package com.ecommerce.shippingservice.repository;

import com.ecommerce.shippingservice.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByOrderId(String orderId);
    Optional<Shipment> findByShipmentId(String shipmentId);
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    List<Shipment> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    List<Shipment> findByStatusOrderByCreatedAtDesc(Shipment.ShipmentStatus status);
}
