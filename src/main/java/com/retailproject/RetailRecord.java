package com.retailproject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
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
