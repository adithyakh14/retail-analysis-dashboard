package com.retailproject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomerDimension {
    private final int customerKey;
    private final String customerName;
}
