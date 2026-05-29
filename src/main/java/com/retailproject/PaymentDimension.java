package com.retailproject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentDimension {
    private final int paymentKey;
    private final String paymentMethod;
}
