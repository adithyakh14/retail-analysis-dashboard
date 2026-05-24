package com.retailproject;

public class StatusDimension {
    private final int statusKey;
    private final String orderStatus;

    public StatusDimension(int statusKey, String orderStatus) {
        this.statusKey = statusKey;
        this.orderStatus = orderStatus;
    }

    public int getStatusKey() {
        return statusKey;
    }

    public String getOrderStatus() {
        return orderStatus;
    }
}
