package com.ecommerce.inventoryservice.service;

import com.ecommerce.inventoryservice.exception.InventoryException;
import com.ecommerce.inventoryservice.kafka.InventoryEventProducer;
import com.ecommerce.inventoryservice.kafka.KafkaTopics;
import com.ecommerce.inventoryservice.model.InventoryReservation;
import com.ecommerce.inventoryservice.model.Product;
import com.ecommerce.inventoryservice.repository.InventoryReservationRepository;
import com.ecommerce.inventoryservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    @Autowired private ProductRepository productRepository;
    @Autowired private InventoryReservationRepository reservationRepository;
    @Autowired private InventoryEventProducer producer;

    @Transactional
    public void reserveInventory(Map<String, Object> orderEvent) {
        String orderId = (String) orderEvent.get("orderId");
        List<Map<String, Object>> items = (List<Map<String, Object>>) orderEvent.get("items");

        List<InventoryReservation> reservations = new ArrayList<>();

        for (Map<String, Object> item : items) {
            String sku = (String) item.get("sku");
            int qty = ((Number) item.get("quantity")).intValue();

            Optional<Product> productOpt = productRepository.findBySku(sku);
            if (productOpt.isEmpty() || productOpt.get().getStockQuantity() < qty) {
                // Release any reservations already made
                reservations.forEach(r -> {
                    productRepository.findBySku(r.getSku()).ifPresent(p -> {
                        p.setStockQuantity(p.getStockQuantity() + r.getQuantity());
                        productRepository.save(p);
                    });
                    r.setStatus(InventoryReservation.ReservationStatus.RELEASED);
                    reservationRepository.save(r);
                });

                Map<String, Object> event = new HashMap<>();
                event.put("eventType", "INVENTORY_INSUFFICIENT");
                event.put("orderId", orderId);
                event.put("reason", "Insufficient stock for SKU: " + sku);
                producer.publish(KafkaTopics.INVENTORY_INSUFFICIENT, event);
                log.warn("Inventory insufficient for order {} sku {}", orderId, sku);
                return;
            }

            Product product = productOpt.get();
            product.setStockQuantity(product.getStockQuantity() - qty);
            productRepository.save(product);

            InventoryReservation reservation = new InventoryReservation();
            reservation.setReservationId("RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            reservation.setOrderId(orderId);
            reservation.setSku(sku);
            reservation.setQuantity(qty);
            reservation.setStatus(InventoryReservation.ReservationStatus.RESERVED);
            reservations.add(reservationRepository.save(reservation));
        }

        Map<String, Object> event = new HashMap<>(orderEvent);
        event.put("eventType", "INVENTORY_RESERVED");
        producer.publish(KafkaTopics.INVENTORY_RESERVED, event);
        log.info("Inventory reserved for order {}", orderId);
    }

    @Transactional
    public void releaseReservation(String orderId) {
        List<InventoryReservation> reservations = reservationRepository.findByOrderId(orderId);
        for (InventoryReservation r : reservations) {
            if (r.getStatus() == InventoryReservation.ReservationStatus.RESERVED) {
                productRepository.findBySku(r.getSku()).ifPresent(p -> {
                    p.setStockQuantity(p.getStockQuantity() + r.getQuantity());
                    productRepository.save(p);
                });
                r.setStatus(InventoryReservation.ReservationStatus.RELEASED);
                reservationRepository.save(r);
            }
        }
        log.info("Inventory released for order {}", orderId);
    }

    @Transactional
    public void consumeReservation(String orderId) {
        reservationRepository.findByOrderId(orderId).forEach(r -> {
            if (r.getStatus() == InventoryReservation.ReservationStatus.RESERVED) {
                r.setStatus(InventoryReservation.ReservationStatus.CONSUMED);
                reservationRepository.save(r);
            }
        });
    }

    public List<Product> getAllProducts() { return productRepository.findByActiveTrue(); }
    public List<Product> getLowStock() { return productRepository.findByStockQuantityLessThanEqualAndActiveTrue(10); }

    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new InventoryException("Product not found: " + sku));
    }

    @Transactional
    public Product updateStock(String sku, int delta) {
        Product p = getProductBySku(sku);
        if (p.getStockQuantity() + delta < 0) throw new InventoryException("Insufficient stock");
        p.setStockQuantity(p.getStockQuantity() + delta);
        return productRepository.save(p);
    }

    @Transactional
    public Product createProduct(Product product) { return productRepository.save(product); }
}
