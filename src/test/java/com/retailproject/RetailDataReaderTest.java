package com.retailproject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RetailDataReaderTest {
    private RetailDataReader retailDataReader;
    private List<RetailRecord> sampleRecords;

    @BeforeEach
    void setUp() {
        retailDataReader = new RetailDataReader();
        sampleRecords = List.of(
                new RetailRecord(1001, "2026-01-01", "Alice", "Mumbai", "Laptop", "Electronics", 50000.0, 1, 5.0, "Card", "Delivered"),
                new RetailRecord(1002, "2026-01-02", "Bob", "Delhi", "Chair", "Furniture", 10000.0, 2, 0.0, "Cash", "Pending"),
                new RetailRecord(1003, "2026-01-03", "Alice", "Mumbai", "Chair", "Furniture", 5000.0, 1, 10.0, "Card", "Delivered"));
    }

    @Test
    void readRecordsFromResourceLoadsRetailCsv() throws IOException {
        List<RetailRecord> records = retailDataReader.readRecordsFromResource("retail.csv");

        assertEquals(500, records.size());
        assertEquals(1001, records.getFirst().getOrderId());
    }

    @Test
    void missingResourceThrowsIOException() {
        assertThrows(IOException.class, () -> retailDataReader.readRecordsFromResource("missing.csv"));
    }

    @Test
    void uniqueCustomersAndProductsAreCollectedAlphabetically() {
        assertEquals(Set.of("Alice", "Bob"), retailDataReader.getUniqueCustomers(sampleRecords));
        assertEquals(Set.of("Chair", "Laptop"), retailDataReader.getUniqueProducts(sampleRecords));
    }

    @Test
    void totalSalesAndSalesByProductAreCalculatedCorrectly() {
        assertEquals(72000.0, retailDataReader.getTotalSales(sampleRecords));
        assertEquals(
                Map.of("Chair", 24500.0, "Laptop", 47500.0),
                retailDataReader.getSalesByProduct(sampleRecords));
    }

    @Test
    void customerSpecificOrdersAndGroupedOrdersAreReturned() {
        assertEquals(2, retailDataReader.getOrdersByCustomer(sampleRecords, "alice").size());
        assertEquals(1, retailDataReader.getOrdersByCustomer(sampleRecords, "bob").size());

        Map<String, List<RetailRecord>> ordersForEachCustomer = retailDataReader.getOrdersForEachCustomer(sampleRecords);
        assertEquals(2, ordersForEachCustomer.get("Alice").size());
        assertEquals(1, ordersForEachCustomer.get("Bob").size());
    }
}
