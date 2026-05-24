package com.retailproject;

public class RetailRecord {
    private final int orderId;
    private final String orderDate;
    private final String customer;
    private final String city;
    private final String product;
    private final String category;
    private final double price;
    private final int quantity;
    private final double discount;
    private final String paymentMethod;
    private final String orderStatus;

    public RetailRecord(
            int orderId,
            String orderDate,
            String customer,
            String city,
            String product,
            String category,
            double price,
            int quantity,
            double discount,
            String paymentMethod,
            String orderStatus) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.customer = customer;
        this.city = city;
        this.product = product;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.discount = discount;
        this.paymentMethod = paymentMethod;
        this.orderStatus = orderStatus;
    }

    public String getCustomer() {
        return customer;
    }

    public String getProduct() {
        return product;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public String getCity() {
        return city;
    }

    public String getCategory() {
        return category;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public double getSalesAmount() {
        return price * quantity * (1 - discount / 100.0);
    }

    public String toCsvRow(String section) {
        return String.join(",",
                section,
                String.valueOf(orderId),
                orderDate,
                customer,
                city,
                product,
                category,
                String.valueOf(price),
                String.valueOf(quantity),
                String.valueOf(discount),
                paymentMethod,
                orderStatus,
                String.valueOf(getSalesAmount()));
    }

    @Override
    public String toString() {
        return String.format(
                "OrderID=%d, Date=%s, Customer=%s, City=%s, Product=%s, Category=%s, Price=%.2f, Quantity=%d, Discount=%.2f%%, Payment=%s, Status=%s, Sales=%.2f",
                orderId,
                orderDate,
                customer,
                city,
                product,
                category,
                price,
                quantity,
                discount,
                paymentMethod,
                orderStatus,
                getSalesAmount());
    }
}
