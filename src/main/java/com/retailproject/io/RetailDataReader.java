package com.retailproject.io;

import com.retailproject.model.RetailRecord;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.springframework.stereotype.Component;

@Component
public class RetailDataReader {
    public List<RetailRecord> readRecordsFromResource(String resourceName) throws IOException {
        List<RetailRecord> records = new ArrayList<>();

        try (InputStream inputStream = getRequiredResource(resourceName);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            reader.readLine();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                records.add(parseRecord(line));
            }
        } catch (NumberFormatException e) {
            throw new IOException("Invalid number in CSV file", e);
        }

        return records;
    }

    public Set<String> getUniqueCustomers(List<RetailRecord> records) {
        Set<String> uniqueCustomers = new TreeSet<>();

        for (RetailRecord record : records) {
            uniqueCustomers.add(record.getCustomer());
        }

        return uniqueCustomers;
    }

    public double getTotalSales(List<RetailRecord> records) {
        double totalSales = 0;

        for (RetailRecord record : records) {
            totalSales += record.getSalesAmount();
        }

        return totalSales;
    }

    public Set<String> getUniqueProducts(List<RetailRecord> records) {
        Set<String> uniqueProducts = new TreeSet<>();

        for (RetailRecord record : records) {
            uniqueProducts.add(record.getProduct());
        }

        return uniqueProducts;
    }

    public List<RetailRecord> getOrdersByCustomer(List<RetailRecord> records, String customerName) {
        List<RetailRecord> customerOrders = new ArrayList<>();

        for (RetailRecord record : records) {
            if (record.getCustomer().equalsIgnoreCase(customerName)) {
                customerOrders.add(record);
            }
        }

        return customerOrders;
    }

    public Map<String, Double> getSalesByProduct(List<RetailRecord> records) {
        Map<String, Double> salesByProduct = new TreeMap<>();

        for (RetailRecord record : records) {
            String product = record.getProduct();
            double updatedSales = salesByProduct.getOrDefault(product, 0.0) + record.getSalesAmount();
            salesByProduct.put(product, updatedSales);
        }

        return salesByProduct;
    }

    public Map<String, List<RetailRecord>> getOrdersForEachCustomer(List<RetailRecord> records) {
        Map<String, List<RetailRecord>> ordersByCustomer = new TreeMap<>();

        for (RetailRecord record : records) {
            String customer = record.getCustomer();
            ordersByCustomer.computeIfAbsent(customer, key -> new ArrayList<>()).add(record);
        }

        return ordersByCustomer;
    }

    private InputStream getRequiredResource(String resourceName) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName);
        if (inputStream == null) {
            throw new IOException("Resource not found: " + resourceName);
        }
        return inputStream;
    }

    private RetailRecord parseRecord(String line) throws IOException {
        String[] values = line.split(",");
        if (values.length != 11) {
            throw new IOException("Invalid CSV row: " + line);
        }

        return new RetailRecord(
                Integer.parseInt(values[0].trim()),
                values[1].trim(),
                values[2].trim(),
                values[3].trim(),
                values[4].trim(),
                values[5].trim(),
                Double.parseDouble(values[6].trim()),
                Integer.parseInt(values[7].trim()),
                Double.parseDouble(values[8].trim()),
                values[9].trim(),
                values[10].trim());
    }
}
