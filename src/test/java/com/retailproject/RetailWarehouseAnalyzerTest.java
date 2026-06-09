package com.retailproject;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.retailproject.model.CityDimension;
import com.retailproject.model.CustomerDimension;
import com.retailproject.model.DateDimension;
import com.retailproject.model.PaymentDimension;
import com.retailproject.model.ProductDimension;
import com.retailproject.model.RetailWarehouse;
import com.retailproject.model.SalesFact;
import com.retailproject.model.StatusDimension;
import com.retailproject.warehouse.RetailWarehouseAnalyzer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RetailWarehouseAnalyzerTest {
    private RetailWarehouseAnalyzer analyzer;
    private RetailWarehouse warehouse;

    @BeforeEach
    void setUp() {
        analyzer = new RetailWarehouseAnalyzer();
        warehouse = new RetailWarehouse(
                List.of(
                        new CustomerDimension(1, "Alice"),
                        new CustomerDimension(2, "Bob")),
                List.of(
                        new ProductDimension(1, "Laptop", "Electronics"),
                        new ProductDimension(2, "Chair", "Furniture")),
                List.of(
                        new DateDimension(1, "2026-01-01", 2026, 1, 1, "Thursday"),
                        new DateDimension(2, "2026-01-02", 2026, 1, 1, "Friday")),
                List.of(
                        new CityDimension(1, "Mumbai"),
                        new CityDimension(2, "Delhi")),
                List.of(
                        new PaymentDimension(1, "Card"),
                        new PaymentDimension(2, "Cash")),
                List.of(
                        new StatusDimension(1, "Delivered"),
                        new StatusDimension(2, "Pending")),
                List.of(
                        new SalesFact(1001, 1, 1, 1, 1, 1, 1, 50000.0, 1, 5.0, 47500.0),
                        new SalesFact(1002, 1, 2, 2, 2, 2, 2, 10000.0, 2, 0.0, 20000.0),
                        new SalesFact(1003, 2, 1, 2, 1, 1, 1, 5000.0, 1, 10.0, 4500.0)));
    }

    @Test
    void getTotalSalesAddsAllSalesFacts() {
        assertEquals(72000.0, analyzer.getTotalSales(warehouse));
    }

    @Test
    void uniqueCustomersAndProductsAreReturnedAlphabetically() {
        assertEquals(Set.of("Alice", "Bob"), analyzer.getUniqueCustomers(warehouse));
        assertEquals(Set.of("Chair", "Laptop"), analyzer.getUniqueProducts(warehouse));
    }

    @Test
    void salesAreGroupedByCityAndPaymentMethod() {
        assertEquals(
                Map.of("Delhi", 20000.0, "Mumbai", 52000.0),
                analyzer.getSalesByCity(warehouse));

        assertEquals(
                Map.of("Card", 52000.0, "Cash", 20000.0),
                analyzer.getSalesByPaymentMethod(warehouse));
    }

    @Test
    void orderCountsAndSalesAreGroupedByStatus() {
        assertEquals(
                Map.of("Delivered", 2, "Pending", 1),
                analyzer.getOrderCountByStatus(warehouse));

        assertEquals(
                Map.of("Delivered", 52000.0, "Pending", 20000.0),
                analyzer.getSalesByStatus(warehouse));
    }

    @Test
    void salesTrendAndProductTotalsAreCalculatedCorrectly() {
        assertEquals(
                Map.of("2026-01-01", 67500.0, "2026-01-02", 4500.0),
                analyzer.getSalesTrendByDate(warehouse));

        assertEquals(
                Map.of("Chair", 24500.0, "Laptop", 47500.0),
                analyzer.getSalesByProduct(warehouse));
    }
}
