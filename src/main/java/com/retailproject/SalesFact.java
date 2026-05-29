package com.retailproject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
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
}
