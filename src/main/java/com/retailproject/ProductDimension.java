package com.retailproject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ProductDimension {
    private final int productKey;
    private final String productName;
    private final String category;
}
