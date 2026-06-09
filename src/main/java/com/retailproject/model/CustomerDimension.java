package com.retailproject.model;

public record CustomerDimension(int customerKey, String customerName) {
    public int getCustomerKey() {
        return customerKey;
    }

    public String getCustomerName() {
        return customerName;
    }
}
