package com.ecommerce.shippingservice.controller;

import com.ecommerce.shippingservice.model.Shipment;
import com.ecommerce.shippingservice.service.ShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shipments")
public class ShippingController {

    @Autowired private ShippingService shippingService;

    @GetMapping
    public ResponseEntity<List<Shipment>> getAll() {
        return ResponseEntity.ok(shippingService.getAll());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Shipment> getByOrderId(@PathVariable String orderId) {
        return ResponseEntity.ok(shippingService.getByOrderId(orderId));
    }

    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<Shipment> getByTracking(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(shippingService.getByTracking(trackingNumber));
    }

    @PutMapping("/{shipmentId}/dispatch")
    public ResponseEntity<Shipment> dispatch(@PathVariable String shipmentId) {
        return ResponseEntity.ok(shippingService.dispatch(shipmentId));
    }

    @PutMapping("/{shipmentId}/deliver")
    public ResponseEntity<Shipment> deliver(@PathVariable String shipmentId) {
        return ResponseEntity.ok(shippingService.deliver(shipmentId));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "shipping-service"));
    }
}
