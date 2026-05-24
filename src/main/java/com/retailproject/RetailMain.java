package com.retailproject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class RetailMain {
    public static void main(String[] args) {
        RetailDataReader dataReader = new RetailDataReader();
        RetailResultsWriter resultsWriter = new RetailResultsWriter();
        RetailWarehouseBuilder warehouseBuilder = new RetailWarehouseBuilder();
        RetailWarehouseAnalyzer warehouseAnalyzer = new RetailWarehouseAnalyzer();
        RetailWarehouseWriter warehouseWriter = new RetailWarehouseWriter();
        Path outputDirectory = resolveOutputDirectory();
        Path resultsPath = outputDirectory.resolve("RetailResults.csv");

        try {
            Files.createDirectories(outputDirectory);
            List<RetailRecord> records = dataReader.readRecordsFromResource("retail.csv");
            RetailWarehouse warehouse = warehouseBuilder.buildWarehouse(records);
            List<RetailRecord> detailedRecords = warehouseAnalyzer.getDetailedRecords(warehouse);
            Set<String> uniqueCustomers = warehouseAnalyzer.getUniqueCustomers(warehouse);
            Set<String> uniqueProducts = warehouseAnalyzer.getUniqueProducts(warehouse);
            double totalSales = warehouseAnalyzer.getTotalSales(warehouse);
            Map<String, Double> salesByProduct = warehouseAnalyzer.getSalesByProduct(warehouse);
            Map<String, List<RetailRecord>> ordersByCustomer = warehouseAnalyzer.getOrdersByCustomer(warehouse);
            Map<String, Double> salesByCity = warehouseAnalyzer.getSalesByCity(warehouse);
            Map<String, Double> salesByPaymentMethod = warehouseAnalyzer.getSalesByPaymentMethod(warehouse);
            Map<String, Double> salesTrendByDate = warehouseAnalyzer.getSalesTrendByDate(warehouse);
            Map<String, Integer> orderCountByStatus = warehouseAnalyzer.getOrderCountByStatus(warehouse);
            Map<String, Double> salesByStatus = warehouseAnalyzer.getSalesByStatus(warehouse);
            runMenu(
                    detailedRecords,
                    uniqueCustomers,
                    uniqueProducts,
                    totalSales,
                    salesByProduct,
                    ordersByCustomer,
                    salesByCity,
                    salesByPaymentMethod,
                    salesTrendByDate,
                    orderCountByStatus,
                    salesByStatus,
                    warehouse,
                    resultsWriter,
                    resultsPath,
                    warehouseWriter,
                    outputDirectory);
        } catch (IOException e) {
            System.out.println("Error reading CSV file: " + e.getMessage());
        }
    }

    private static void runMenu(
            List<RetailRecord> records,
            Set<String> uniqueCustomers,
            Set<String> uniqueProducts,
            double totalSales,
            Map<String, Double> salesByProduct,
            Map<String, List<RetailRecord>> ordersByCustomer,
            Map<String, Double> salesByCity,
            Map<String, Double> salesByPaymentMethod,
            Map<String, Double> salesTrendByDate,
            Map<String, Integer> orderCountByStatus,
            Map<String, Double> salesByStatus,
            RetailWarehouse warehouse,
            RetailResultsWriter resultsWriter,
            Path resultsPath,
            RetailWarehouseWriter warehouseWriter,
            Path warehouseOutputDirectory) throws IOException {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;

            while (running) {
                printMenu();
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        printSummary(records, uniqueCustomers, uniqueProducts, totalSales);
                        break;
                    case "2":
                        printSalesByProduct(salesByProduct);
                        break;
                    case "3":
                        printOrdersForEachCustomer(ordersByCustomer);
                        break;
                    case "4":
                        resultsWriter.writeResults(
                                resultsPath.toString(),
                                records,
                                uniqueCustomers,
                                uniqueProducts,
                                totalSales,
                                salesByProduct,
                                ordersByCustomer,
                                salesByCity,
                                salesByPaymentMethod,
                                salesTrendByDate,
                                orderCountByStatus,
                                salesByStatus);
                        System.out.println();
                        System.out.println("Results written to: " + resultsPath);
                        System.out.println();
                        break;
                    case "5":
                        printWarehouseSummary(warehouse);
                        printWarehouseAnalytics(
                                salesByCity,
                                salesByPaymentMethod,
                                salesTrendByDate,
                                orderCountByStatus,
                                salesByStatus);
                        break;
                    case "6":
                        warehouseWriter.writeWarehouseTables(warehouseOutputDirectory, warehouse);
                        System.out.println();
                        System.out.println("Warehouse tables written to: " + warehouseOutputDirectory);
                        System.out.println();
                        break;
                    case "7":
                        printSummary(records, uniqueCustomers, uniqueProducts, totalSales);
                        printSalesByProduct(salesByProduct);
                        printOrdersForEachCustomer(ordersByCustomer);
                        printWarehouseSummary(warehouse);
                        printWarehouseAnalytics(
                                salesByCity,
                                salesByPaymentMethod,
                                salesTrendByDate,
                                orderCountByStatus,
                                salesByStatus);
                        resultsWriter.writeResults(
                                resultsPath.toString(),
                                records,
                                uniqueCustomers,
                                uniqueProducts,
                                totalSales,
                                salesByProduct,
                                ordersByCustomer,
                                salesByCity,
                                salesByPaymentMethod,
                                salesTrendByDate,
                                orderCountByStatus,
                                salesByStatus);
                        warehouseWriter.writeWarehouseTables(warehouseOutputDirectory, warehouse);
                        System.out.println("Results written to: " + resultsPath);
                        System.out.println("Warehouse tables written to: " + warehouseOutputDirectory);
                        System.out.println();
                        break;
                    case "0":
                        running = false;
                        System.out.println("Exiting RetailProject.");
                        break;
                    default:
                        System.out.println();
                        System.out.println("Invalid choice. Please select a menu option.");
                        System.out.println();
                        break;
                }
            }
        }
    }

    private static void printMenu() {
        System.out.println("Retail Project Menu");
        System.out.println("1. View summary");
        System.out.println("2. View sales breakdown by product");
        System.out.println("3. View orders for each customer");
        System.out.println("4. Export results to RetailResults.csv");
        System.out.println("5. View warehouse analytics");
        System.out.println("6. Export dimension and fact tables");
        System.out.println("7. Run everything");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }

    private static void printSummary(
            List<RetailRecord> records,
            Set<String> uniqueCustomers,
            Set<String> uniqueProducts,
            double totalSales) {
        System.out.println();
        System.out.println("Retail records:");
        for (RetailRecord record : records) {
            System.out.println("  " + record);
        }
        System.out.println();
        System.out.println("Unique customers: " + uniqueCustomers);
        System.out.println("Unique products: " + uniqueProducts);
        System.out.println("Total sales: " + String.format("%.2f", totalSales));
        System.out.println();
    }

    private static void printSalesByProduct(Map<String, Double> salesByProduct) {
        System.out.println();
        System.out.println("Sales breakdown by product:");
        for (Map.Entry<String, Double> entry : salesByProduct.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + String.format("%.2f", entry.getValue()));
        }
        System.out.println();
    }

    private static void printOrdersForEachCustomer(Map<String, List<RetailRecord>> ordersByCustomer) {
        System.out.println();
        System.out.println("Orders for each customer:");
        for (Map.Entry<String, List<RetailRecord>> entry : ordersByCustomer.entrySet()) {
            System.out.println(entry.getKey() + ":");
            for (RetailRecord record : entry.getValue()) {
                System.out.println("  " + record);
            }
            System.out.println();
        }
    }

    private static void printWarehouseSummary(RetailWarehouse warehouse) {
        System.out.println();
        System.out.println("Warehouse summary:");
        System.out.println("  DimCustomer rows: " + warehouse.getCustomers().size());
        System.out.println("  DimProduct rows: " + warehouse.getProducts().size());
        System.out.println("  DimDate rows: " + warehouse.getDates().size());
        System.out.println("  DimCity rows: " + warehouse.getCities().size());
        System.out.println("  DimPayment rows: " + warehouse.getPayments().size());
        System.out.println("  DimStatus rows: " + warehouse.getStatuses().size());
        System.out.println("  FactSales rows: " + warehouse.getSalesFacts().size());
        System.out.println();
    }

    private static void printWarehouseAnalytics(
            Map<String, Double> salesByCity,
            Map<String, Double> salesByPaymentMethod,
            Map<String, Double> salesTrendByDate,
            Map<String, Integer> orderCountByStatus,
            Map<String, Double> salesByStatus) {
        System.out.println("Sales by city:");
        for (Map.Entry<String, Double> entry : salesByCity.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + String.format("%.2f", entry.getValue()));
        }
        System.out.println();

        System.out.println("Sales by payment method:");
        for (Map.Entry<String, Double> entry : salesByPaymentMethod.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + String.format("%.2f", entry.getValue()));
        }
        System.out.println();

        System.out.println("Sales trend by date:");
        for (Map.Entry<String, Double> entry : salesTrendByDate.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + String.format("%.2f", entry.getValue()));
        }
        System.out.println();

        System.out.println("Order count by status:");
        for (Map.Entry<String, Integer> entry : orderCountByStatus.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
        System.out.println();

        System.out.println("Sales by status:");
        for (Map.Entry<String, Double> entry : salesByStatus.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + String.format("%.2f", entry.getValue()));
        }
        System.out.println();
    }

    private static Path resolveOutputDirectory() {
        Path current = Path.of("").toAbsolutePath();

        while (current != null) {
            if (Files.exists(current.resolve("pom.xml"))) {
                return current.resolve("output");
            }

            Path childProjectPom = current.resolve("RetailProject").resolve("pom.xml");
            if (Files.exists(childProjectPom)) {
                return current.resolve("RetailProject").resolve("output");
            }

            current = current.getParent();
        }

        return Path.of("output").toAbsolutePath();
    }
}
