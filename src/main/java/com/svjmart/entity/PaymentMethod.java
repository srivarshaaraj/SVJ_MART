package com.svjmart.entity;

public enum PaymentMethod {
    UPI_QR("UPI QR / Apps (GPay, PhonePe, Paytm)"),
    SIMULATED_CARD("Debit / Credit Card (Simulated Sandbox)"),
    SIMULATED_NETBANKING("Net Banking (Simulated Sandbox)"),
    CASH_ON_DELIVERY("Cash on Delivery (Pay upon delivery)");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
