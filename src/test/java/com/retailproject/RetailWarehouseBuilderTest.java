package com.retailproject;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RetailWarehouseBuilderTest {
    private RetailWarehouseBuilder retailWarehouseBuilder;

    @BeforeEach
    void setUp() {
        retailWarehouseBuilder = new RetailWarehouseBuilder();
    }

    @Test
    void buildWarehouseCreatesExpectedDimensionCounts() {
        RetailWarehouse warehouse = retailWarehouseBuilder.buildWarehouse(sampleRecords());

        assertEquals(2, warehouse.getCustomers().size());
        assertEquals(2, warehouse.getProducts().size());
        assertEquals(2, warehouse.getDates().size());
        assertEquals(2, warehouse.getCities().size());
        assertEquals(2, warehouse.getPayments().size());
        assertEquals(2, warehouse.getStatuses().size());
        assertEquals(3, warehouse.getSalesFacts().size());
    }

    @Test
    void buildWarehouseReusesKeysForRepeatedNaturalValues() {
        RetailWarehouse warehouse = retailWarehouseBuilder.buildWarehouse(sampleRecords());

        SalesFact firstFact = warehouse.getSalesFacts().get(0);
        SalesFact thirdFact = warehouse.getSalesFacts().get(2);

        assertEquals(firstFact.getCustomerKey(), thirdFact.getCustomerKey());
        assertEquals(firstFact.getCityKey(), thirdFact.getCityKey());
        assertEquals(firstFact.getPaymentKey(), thirdFact.getPaymentKey());
        assertEquals(firstFact.getStatusKey(), thirdFact.getStatusKey());
        assertEquals(2, thirdFact.getProductKey());
    }

    @Test
    void buildWarehouseCreatesDateDimensionWithCalendarBreakdown() {
        RetailWarehouse warehouse = retailWarehouseBuilder.buildWarehouse(sampleRecords());

        DateDimension firstDate = warehouse.getDates().get(0);
        DateDimension secondDate = warehouse.getDates().get(1);

        assertEquals("2026-01-01", firstDate.getOrderDate());
        assertEquals(2026, firstDate.getYear());
        assertEquals(1, firstDate.getMonth());
        assertEquals(1, firstDate.getQuarter());
        assertEquals("THURSDAY", firstDate.getDayOfWeek());

        assertEquals("2026-04-10", secondDate.getOrderDate());
        assertEquals(2, secondDate.getQuarter());
        assertEquals("FRIDAY", secondDate.getDayOfWeek());
    }

    private List<RetailRecord> sampleRecords() {
        return List.of(
                new RetailRecord(1001, "2026-01-01", "Alice", "Mumbai", "Laptop", "Electronics", 50000.0, 1, 5.0, "Card", "Delivered"),
                new RetailRecord(1002, "2026-04-10", "Bob", "Delhi", "Chair", "Furniture", 10000.0, 2, 0.0, "Cash", "Pending"),
                new RetailRecord(1003, "2026-01-01", "Alice", "Mumbai", "Chair", "Furniture", 5000.0, 1, 10.0, "Card", "Delivered"));
    }
}
