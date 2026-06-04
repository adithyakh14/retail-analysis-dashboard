package com.retailproject;

import java.util.List;

public record RetailWarehouse(
        List<CustomerDimension> customers,
        List<ProductDimension> products,
        List<DateDimension> dates,
        List<CityDimension> cities,
        List<PaymentDimension> payments,
        List<StatusDimension> statuses,
        List<SalesFact> salesFacts) {
    public RetailWarehouse {
        customers = List.copyOf(customers);
        products = List.copyOf(products);
        dates = List.copyOf(dates);
        cities = List.copyOf(cities);
        payments = List.copyOf(payments);
        statuses = List.copyOf(statuses);
        salesFacts = List.copyOf(salesFacts);
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
