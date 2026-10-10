package com.svjmart.entity;

public enum OrderStatus {
    PLACED("Order Placed", "Order received and pending store confirmation"),
    CONFIRMED("Confirmed", "Order confirmed by SVJ Mart"),
    PACKED("Packed & Ready", "Packed and prepared for dispatch"),
    OUT_FOR_DELIVERY("Out for Delivery", "Our delivery agent is on the way"),
    DELIVERED("Delivered", "Delivered successfully"),
    CANCELLED("Cancelled", "Order cancelled");

    private final String displayName;
    private final String description;

    OrderStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
