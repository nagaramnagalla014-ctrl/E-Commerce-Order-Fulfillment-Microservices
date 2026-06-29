package com.ecommerce.inventoryservice.repository;

import com.ecommerce.inventoryservice.model.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {
    List<InventoryReservation> findByOrderId(String orderId);
    Optional<InventoryReservation> findByReservationId(String reservationId);
    List<InventoryReservation> findBySkuAndStatus(String sku, InventoryReservation.ReservationStatus status);
}
