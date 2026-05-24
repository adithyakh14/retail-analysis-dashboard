package com.retailproject;

public class SalesFact {
    private final int orderId;
    private final int dateKey;
    private final int customerKey;
    private final int productKey;
    private final int cityKey;
    private final int paymentKey;
    private final int statusKey;
    private final double price;
    private final int quantity;
    private final double discount;
    private final double salesAmount;

    public SalesFact(
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
        this.orderId = orderId;
        this.dateKey = dateKey;
        this.customerKey = customerKey;
        this.productKey = productKey;
        this.cityKey = cityKey;
        this.paymentKey = paymentKey;
        this.statusKey = statusKey;
        this.price = price;
        this.quantity = quantity;
        this.discount = discount;
        this.salesAmount = salesAmount;
    }

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
