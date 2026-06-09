package com.retailproject.model;

public record StatusDimension(int statusKey, String orderStatus) {
    public int getStatusKey() {
        return statusKey;
    }

    public String getOrderStatus() {
        return orderStatus;
    }
}
