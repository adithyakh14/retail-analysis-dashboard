package com.retailproject;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RetailWarehouse {
    private final List<CustomerDimension> customers;
    private final List<ProductDimension> products;
    private final List<DateDimension> dates;
    private final List<CityDimension> cities;
    private final List<PaymentDimension> payments;
    private final List<StatusDimension> statuses;
    private final List<SalesFact> salesFacts;
}
