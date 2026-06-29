package com.ecommerce.notificationservice.kafka;

public class KafkaTopics {
    public static final String ORDER_CREATED = "order-created";
    public static final String INVENTORY_INSUFFICIENT = "inventory-insufficient";
    public static final String PAYMENT_COMPLETED = "payment-completed";
    public static final String PAYMENT_FAILED = "payment-failed";
    public static final String SHIPMENT_CREATED = "shipment-created";
    public static final String SHIPMENT_DISPATCHED = "shipment-dispatched";
    public static final String SHIPMENT_DELIVERED = "shipment-delivered";
}
