package com.retailproject;

public record RetailRecord(
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
    public int getOrderId() {
        return orderId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public String getCustomer() {
        return customer;
    }

    public String getCity() {
        return city;
    }

    public String getProduct() {
        return product;
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
