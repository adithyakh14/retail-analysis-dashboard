package com.retailproject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class RetailWarehouseAnalyzer {
    public List<RetailRecord> getDetailedRecords(RetailWarehouse warehouse) {
        Map<Integer, String> customerNames = getCustomerNames(warehouse);
        Map<Integer, ProductDimension> products = getProducts(warehouse);
        Map<Integer, String> orderDates = getOrderDates(warehouse);
        Map<Integer, String> cityNames = getCityNames(warehouse);
        Map<Integer, String> paymentMethods = getPaymentMethods(warehouse);
        Map<Integer, String> statuses = getStatuses(warehouse);

        List<RetailRecord> records = new ArrayList<>();
        for (SalesFact fact : warehouse.getSalesFacts()) {
            ProductDimension product = products.get(fact.getProductKey());
            records.add(new RetailRecord(
                    fact.getOrderId(),
                    orderDates.get(fact.getDateKey()),
                    customerNames.get(fact.getCustomerKey()),
                    cityNames.get(fact.getCityKey()),
                    product.getProductName(),
                    product.getCategory(),
                    fact.getPrice(),
                    fact.getQuantity(),
                    fact.getDiscount(),
                    paymentMethods.get(fact.getPaymentKey()),
                    statuses.get(fact.getStatusKey())));
        }

        return records;
    }

    public Set<String> getUniqueCustomers(RetailWarehouse warehouse) {
        Set<String> uniqueCustomers = new TreeSet<>();
        for (CustomerDimension customer : warehouse.getCustomers()) {
            uniqueCustomers.add(customer.getCustomerName());
        }
        return uniqueCustomers;
    }

    public Set<String> getUniqueProducts(RetailWarehouse warehouse) {
        Set<String> uniqueProducts = new TreeSet<>();
        for (ProductDimension product : warehouse.getProducts()) {
            uniqueProducts.add(product.getProductName());
        }
        return uniqueProducts;
    }

    public double getTotalSales(RetailWarehouse warehouse) {
        double totalSales = 0;
        for (SalesFact fact : warehouse.getSalesFacts()) {
            totalSales += fact.getSalesAmount();
        }
        return totalSales;
    }

    public Map<String, Double> getSalesByProduct(RetailWarehouse warehouse) {
        Map<Integer, ProductDimension> products = getProducts(warehouse);
        Map<String, Double> salesByProduct = new TreeMap<>();

        for (SalesFact fact : warehouse.getSalesFacts()) {
            String productName = products.get(fact.getProductKey()).getProductName();
            salesByProduct.put(productName, salesByProduct.getOrDefault(productName, 0.0) + fact.getSalesAmount());
        }

        return salesByProduct;
    }

    public Map<String, List<RetailRecord>> getOrdersByCustomer(RetailWarehouse warehouse) {
        Map<String, List<RetailRecord>> ordersByCustomer = new TreeMap<>();

        for (RetailRecord record : getDetailedRecords(warehouse)) {
            ordersByCustomer.computeIfAbsent(record.getCustomer(), key -> new ArrayList<>()).add(record);
        }

        return ordersByCustomer;
    }

    public Map<String, Double> getSalesByCity(RetailWarehouse warehouse) {
        Map<Integer, String> cityNames = getCityNames(warehouse);
        Map<String, Double> salesByCity = new TreeMap<>();

        for (SalesFact fact : warehouse.getSalesFacts()) {
            String cityName = cityNames.get(fact.getCityKey());
            salesByCity.put(cityName, salesByCity.getOrDefault(cityName, 0.0) + fact.getSalesAmount());
        }

        return salesByCity;
    }

    public Map<String, Double> getSalesByPaymentMethod(RetailWarehouse warehouse) {
        Map<Integer, String> paymentMethods = getPaymentMethods(warehouse);
        Map<String, Double> salesByPayment = new TreeMap<>();

        for (SalesFact fact : warehouse.getSalesFacts()) {
            String paymentMethod = paymentMethods.get(fact.getPaymentKey());
            salesByPayment.put(paymentMethod, salesByPayment.getOrDefault(paymentMethod, 0.0) + fact.getSalesAmount());
        }

        return salesByPayment;
    }

    public Map<String, Double> getSalesTrendByDate(RetailWarehouse warehouse) {
        Map<Integer, String> orderDates = getOrderDates(warehouse);
        Map<String, Double> salesTrend = new TreeMap<>();

        for (SalesFact fact : warehouse.getSalesFacts()) {
            String orderDate = orderDates.get(fact.getDateKey());
            salesTrend.put(orderDate, salesTrend.getOrDefault(orderDate, 0.0) + fact.getSalesAmount());
        }

        return salesTrend;
    }

    public Map<String, Integer> getOrderCountByStatus(RetailWarehouse warehouse) {
        Map<Integer, String> statuses = getStatuses(warehouse);
        Map<String, Integer> orderCountByStatus = new TreeMap<>();

        for (SalesFact fact : warehouse.getSalesFacts()) {
            String status = statuses.get(fact.getStatusKey());
            orderCountByStatus.put(status, orderCountByStatus.getOrDefault(status, 0) + 1);
        }

        return orderCountByStatus;
    }

    public Map<String, Double> getSalesByStatus(RetailWarehouse warehouse) {
        Map<Integer, String> statuses = getStatuses(warehouse);
        Map<String, Double> salesByStatus = new TreeMap<>();

        for (SalesFact fact : warehouse.getSalesFacts()) {
            String status = statuses.get(fact.getStatusKey());
            salesByStatus.put(status, salesByStatus.getOrDefault(status, 0.0) + fact.getSalesAmount());
        }

        return salesByStatus;
    }

    private Map<Integer, String> getCustomerNames(RetailWarehouse warehouse) {
        Map<Integer, String> customerNames = new HashMap<>();
        for (CustomerDimension customer : warehouse.getCustomers()) {
            customerNames.put(customer.getCustomerKey(), customer.getCustomerName());
        }
        return customerNames;
    }

    private Map<Integer, ProductDimension> getProducts(RetailWarehouse warehouse) {
        Map<Integer, ProductDimension> products = new HashMap<>();
        for (ProductDimension product : warehouse.getProducts()) {
            products.put(product.getProductKey(), product);
        }
        return products;
    }

    private Map<Integer, String> getOrderDates(RetailWarehouse warehouse) {
        Map<Integer, String> orderDates = new HashMap<>();
        for (DateDimension date : warehouse.getDates()) {
            orderDates.put(date.getDateKey(), date.getOrderDate());
        }
        return orderDates;
    }

    private Map<Integer, String> getCityNames(RetailWarehouse warehouse) {
        Map<Integer, String> cityNames = new HashMap<>();
        for (CityDimension city : warehouse.getCities()) {
            cityNames.put(city.getCityKey(), city.getCityName());
        }
        return cityNames;
    }

    private Map<Integer, String> getPaymentMethods(RetailWarehouse warehouse) {
        Map<Integer, String> paymentMethods = new HashMap<>();
        for (PaymentDimension payment : warehouse.getPayments()) {
            paymentMethods.put(payment.getPaymentKey(), payment.getPaymentMethod());
        }
        return paymentMethods;
    }

    private Map<Integer, String> getStatuses(RetailWarehouse warehouse) {
        Map<Integer, String> statuses = new HashMap<>();
        for (StatusDimension status : warehouse.getStatuses()) {
            statuses.put(status.getStatusKey(), status.getOrderStatus());
        }
        return statuses;
    }
}
