package com.retailproject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RetailWarehouseWriter {
    public void writeWarehouseTables(Path outputDirectory, RetailWarehouse warehouse) throws IOException {
        writeCustomers(outputDirectory.resolve("DimCustomer.csv"), warehouse);
        writeProducts(outputDirectory.resolve("DimProduct.csv"), warehouse);
        writeDates(outputDirectory.resolve("DimDate.csv"), warehouse);
        writeCities(outputDirectory.resolve("DimCity.csv"), warehouse);
        writePayments(outputDirectory.resolve("DimPayment.csv"), warehouse);
        writeStatuses(outputDirectory.resolve("DimStatus.csv"), warehouse);
        writeFacts(outputDirectory.resolve("FactSales.csv"), warehouse);
    }

    private void writeCustomers(Path filePath, RetailWarehouse warehouse) throws IOException {
        writeCsv(filePath, "CustomerKey,CustomerName", warehouse.getCustomers().stream()
                .map(customer -> customer.getCustomerKey() + "," + customer.getCustomerName())
                .toList());
    }

    private void writeProducts(Path filePath, RetailWarehouse warehouse) throws IOException {
        writeCsv(filePath, "ProductKey,ProductName,Category", warehouse.getProducts().stream()
                .map(product -> product.getProductKey() + "," + product.getProductName() + "," + product.getCategory())
                .toList());
    }

    private void writeDates(Path filePath, RetailWarehouse warehouse) throws IOException {
        writeCsv(filePath, "DateKey,OrderDate,Year,Month,Quarter,DayOfWeek", warehouse.getDates().stream()
                .map(date -> date.getDateKey()
                        + "," + date.getOrderDate()
                        + "," + date.getYear()
                        + "," + date.getMonth()
                        + "," + date.getQuarter()
                        + "," + date.getDayOfWeek())
                .toList());
    }

    private void writeCities(Path filePath, RetailWarehouse warehouse) throws IOException {
        writeCsv(filePath, "CityKey,CityName", warehouse.getCities().stream()
                .map(city -> city.getCityKey() + "," + city.getCityName())
                .toList());
    }

    private void writePayments(Path filePath, RetailWarehouse warehouse) throws IOException {
        writeCsv(filePath, "PaymentKey,PaymentMethod", warehouse.getPayments().stream()
                .map(payment -> payment.getPaymentKey() + "," + payment.getPaymentMethod())
                .toList());
    }

    private void writeStatuses(Path filePath, RetailWarehouse warehouse) throws IOException {
        writeCsv(filePath, "StatusKey,OrderStatus", warehouse.getStatuses().stream()
                .map(status -> status.getStatusKey() + "," + status.getOrderStatus())
                .toList());
    }

    private void writeFacts(Path filePath, RetailWarehouse warehouse) throws IOException {
        writeCsv(filePath, "OrderID,DateKey,CustomerKey,ProductKey,CityKey,PaymentKey,StatusKey,Price,Quantity,Discount,SalesAmount",
                warehouse.getSalesFacts().stream()
                        .map(fact -> fact.getOrderId()
                                + "," + fact.getDateKey()
                                + "," + fact.getCustomerKey()
                                + "," + fact.getProductKey()
                                + "," + fact.getCityKey()
                                + "," + fact.getPaymentKey()
                                + "," + fact.getStatusKey()
                                + "," + fact.getPrice()
                                + "," + fact.getQuantity()
                                + "," + fact.getDiscount()
                                + "," + fact.getSalesAmount())
                        .toList());
    }

    private void writeCsv(Path filePath, String header, List<String> rows) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write(header);
            writer.newLine();
            for (String row : rows) {
                writer.write(row);
                writer.newLine();
            }
        }
    }
}
