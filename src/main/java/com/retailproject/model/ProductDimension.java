package com.retailproject.model;

public record ProductDimension(int productKey, String productName, String category) {
    public int getProductKey() {
        return productKey;
    }

    public String getProductName() {
        return productName;
    }

    public String getCategory() {
        return category;
    }
}
