package com.retailproject;

import java.util.List;

public class RetailWarehouse {
    private final List<CustomerDimension> customers;
    private final List<ProductDimension> products;
    private final List<DateDimension> dates;
    private final List<CityDimension> cities;
    private final List<PaymentDimension> payments;
    private final List<StatusDimension> statuses;
    private final List<SalesFact> salesFacts;

    public RetailWarehouse(
            List<CustomerDimension> customers,
            List<ProductDimension> products,
            List<DateDimension> dates,
            List<CityDimension> cities,
            List<PaymentDimension> payments,
            List<StatusDimension> statuses,
            List<SalesFact> salesFacts) {
        this.customers = customers;
        this.products = products;
        this.dates = dates;
        this.cities = cities;
        this.payments = payments;
        this.statuses = statuses;
        this.salesFacts = salesFacts;
    }

    public List<CustomerDimension> getCustomers() {
        return customers;
    }

    public List<ProductDimension> getProducts() {
        return products;
    }

    public List<DateDimension> getDates() {
        return dates;
    }

    public List<CityDimension> getCities() {
        return cities;
    }

    public List<PaymentDimension> getPayments() {
        return payments;
    }

    public List<StatusDimension> getStatuses() {
        return statuses;
    }

    public List<SalesFact> getSalesFacts() {
        return salesFacts;
    }
}
