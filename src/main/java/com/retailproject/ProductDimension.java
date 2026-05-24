package com.retailproject;

public class ProductDimension {
    private final int productKey;
    private final String productName;
    private final String category;

    public ProductDimension(int productKey, String productName, String category) {
        this.productKey = productKey;
        this.productName = productName;
        this.category = category;
    }

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
