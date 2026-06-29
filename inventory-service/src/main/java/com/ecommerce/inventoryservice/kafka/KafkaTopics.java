package com.ecommerce.inventoryservice.kafka;

public class KafkaTopics {
    public static final String ORDER_CREATED = "order-created";
    public static final String ORDER_CONFIRMED = "order-confirmed";
    public static final String INVENTORY_RESERVED = "inventory-reserved";
    public static final String INVENTORY_INSUFFICIENT = "inventory-insufficient";
    public static final String PAYMENT_FAILED = "payment-failed";
}
