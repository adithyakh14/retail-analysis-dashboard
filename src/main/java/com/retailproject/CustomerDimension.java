package com.retailproject;

public class CustomerDimension {
    private final int customerKey;
    private final String customerName;

    public CustomerDimension(int customerKey, String customerName) {
        this.customerKey = customerKey;
        this.customerName = customerName;
    }

    public int getCustomerKey() {
        return customerKey;
    }

    public String getCustomerName() {
        return customerName;
    }
}
