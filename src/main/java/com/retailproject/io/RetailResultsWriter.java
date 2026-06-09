package com.retailproject.io;

import com.retailproject.model.RetailRecord;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class RetailResultsWriter {
    public void writeResults(
            Path filePath,
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
            Map<String, Double> salesByStatus) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write("Section,OrderID,OrderDate,Customer,City,Product,Category,Price,Quantity,Discount,PaymentMethod,OrderStatus,Sales");
            writer.newLine();

            for (RetailRecord record : records) {
                writer.write(record.toCsvRow("RetailRecord"));
                writer.newLine();
            }

            for (String customer : uniqueCustomers) {
                writer.write("UniqueCustomer," + customer);
                writer.newLine();
            }

            for (String product : uniqueProducts) {
                writer.write("UniqueProduct," + product);
                writer.newLine();
            }

            writer.write("TotalSales,,,,,,,,,,,," + String.format("%.2f", totalSales));
            writer.newLine();

            for (Map.Entry<String, Double> entry : salesByProduct.entrySet()) {
                writer.write("SalesByProduct,,,,"
                        + entry.getKey()
                        + ",,,,,,,"
                        + String.format("%.2f", entry.getValue()));
                writer.newLine();
            }

            for (Map.Entry<String, List<RetailRecord>> entry : ordersByCustomer.entrySet()) {
                for (RetailRecord record : entry.getValue()) {
                    writer.write(record.toCsvRow("CustomerOrder"));
                    writer.newLine();
                }
            }

            for (Map.Entry<String, Double> entry : salesByCity.entrySet()) {
                writer.write("SalesByCity,,,"
                        + entry.getKey()
                        + ",,,,,,,,"
                        + String.format("%.2f", entry.getValue()));
                writer.newLine();
            }

            for (Map.Entry<String, Double> entry : salesByPaymentMethod.entrySet()) {
                writer.write("SalesByPaymentMethod,,,,,,,,,"
                        + entry.getKey()
                        + ",,"
                        + String.format("%.2f", entry.getValue()));
                writer.newLine();
            }

            for (Map.Entry<String, Double> entry : salesTrendByDate.entrySet()) {
                writer.write("SalesTrendByDate,,"
                        + entry.getKey()
                        + ",,,,,,,,,"
                        + String.format("%.2f", entry.getValue()));
                writer.newLine();
            }

            for (Map.Entry<String, Integer> entry : orderCountByStatus.entrySet()) {
                writer.write("OrderCountByStatus,,,,,,,,,,"
                        + entry.getKey()
                        + ","
                        + entry.getValue());
                writer.newLine();
            }

            for (Map.Entry<String, Double> entry : salesByStatus.entrySet()) {
                writer.write("SalesByStatus,,,,,,,,,,"
                        + entry.getKey()
                        + ","
                        + String.format("%.2f", entry.getValue()));
                writer.newLine();
            }
        }
    }
}
