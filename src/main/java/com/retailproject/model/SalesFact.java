package com.retailproject.model;

public record SalesFact(
        int orderId,
        int dateKey,
        int customerKey,
        int productKey,
        int cityKey,
        int paymentKey,
        int statusKey,
        double price,
        int quantity,
        double discount,
        double salesAmount) {
    public int getOrderId() {
        return orderId;
    }

    public int getDateKey() {
        return dateKey;
    }

    public int getCustomerKey() {
        return customerKey;
    }

    public int getProductKey() {
        return productKey;
    }

    public int getCityKey() {
        return cityKey;
    }

    public int getPaymentKey() {
        return paymentKey;
    }

    public int getStatusKey() {
        return statusKey;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getDiscount() {
        return discount;
    }

    public double getSalesAmount() {
        return salesAmount;
    }
}
