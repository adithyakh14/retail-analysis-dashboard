package com.retailproject;

public class PaymentDimension {
    private final int paymentKey;
    private final String paymentMethod;

    public PaymentDimension(int paymentKey, String paymentMethod) {
        this.paymentKey = paymentKey;
        this.paymentMethod = paymentMethod;
    }

    public int getPaymentKey() {
        return paymentKey;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }
}
