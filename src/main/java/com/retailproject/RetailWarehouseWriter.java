package com.retailproject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

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
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            writer.write("CustomerKey,CustomerName");
            writer.newLine();
            for (CustomerDimension customer : warehouse.getCustomers()) {
                writer.write(customer.getCustomerKey() + "," + customer.getCustomerName());
                writer.newLine();
            }
        }
    }

    private void writeProducts(Path filePath, RetailWarehouse warehouse) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            writer.write("ProductKey,ProductName,Category");
            writer.newLine();
            for (ProductDimension product : warehouse.getProducts()) {
                writer.write(product.getProductKey() + "," + product.getProductName() + "," + product.getCategory());
                writer.newLine();
            }
        }
    }

    private void writeDates(Path filePath, RetailWarehouse warehouse) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            writer.write("DateKey,OrderDate,Year,Month,Quarter,DayOfWeek");
            writer.newLine();
            for (DateDimension date : warehouse.getDates()) {
                writer.write(date.getDateKey()
                        + "," + date.getOrderDate()
                        + "," + date.getYear()
                        + "," + date.getMonth()
                        + "," + date.getQuarter()
                        + "," + date.getDayOfWeek());
                writer.newLine();
            }
        }
    }

    private void writeCities(Path filePath, RetailWarehouse warehouse) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            writer.write("CityKey,CityName");
            writer.newLine();
            for (CityDimension city : warehouse.getCities()) {
                writer.write(city.getCityKey() + "," + city.getCityName());
                writer.newLine();
            }
        }
    }

    private void writePayments(Path filePath, RetailWarehouse warehouse) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            writer.write("PaymentKey,PaymentMethod");
            writer.newLine();
            for (PaymentDimension payment : warehouse.getPayments()) {
                writer.write(payment.getPaymentKey() + "," + payment.getPaymentMethod());
                writer.newLine();
            }
        }
    }

    private void writeStatuses(Path filePath, RetailWarehouse warehouse) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            writer.write("StatusKey,OrderStatus");
            writer.newLine();
            for (StatusDimension status : warehouse.getStatuses()) {
                writer.write(status.getStatusKey() + "," + status.getOrderStatus());
                writer.newLine();
            }
        }
    }

    private void writeFacts(Path filePath, RetailWarehouse warehouse) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            writer.write("OrderID,DateKey,CustomerKey,ProductKey,CityKey,PaymentKey,StatusKey,Price,Quantity,Discount,SalesAmount");
            writer.newLine();
            for (SalesFact fact : warehouse.getSalesFacts()) {
                writer.write(fact.getOrderId()
                        + "," + fact.getDateKey()
                        + "," + fact.getCustomerKey()
                        + "," + fact.getProductKey()
                        + "," + fact.getCityKey()
                        + "," + fact.getPaymentKey()
                        + "," + fact.getStatusKey()
                        + "," + fact.getPrice()
                        + "," + fact.getQuantity()
                        + "," + fact.getDiscount()
                        + "," + fact.getSalesAmount());
                writer.newLine();
            }
        }
    }
}
