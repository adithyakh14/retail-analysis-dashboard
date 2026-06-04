package com.retailproject;

public record PaymentDimension(int paymentKey, String paymentMethod) {
    public int getPaymentKey() {
        return paymentKey;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }
}
