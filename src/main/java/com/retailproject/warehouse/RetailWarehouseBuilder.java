package com.retailproject.warehouse;

import com.retailproject.model.CityDimension;
import com.retailproject.model.CustomerDimension;
import com.retailproject.model.DateDimension;
import com.retailproject.model.PaymentDimension;
import com.retailproject.model.ProductDimension;
import com.retailproject.model.RetailRecord;
import com.retailproject.model.RetailWarehouse;
import com.retailproject.model.SalesFact;
import com.retailproject.model.StatusDimension;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class RetailWarehouseBuilder {
    public RetailWarehouse buildWarehouse(List<RetailRecord> records) {
        Map<String, Integer> customerKeys = new LinkedHashMap<>();
        Map<String, Integer> productKeys = new LinkedHashMap<>();
        Map<String, Integer> dateKeys = new LinkedHashMap<>();
        Map<String, Integer> cityKeys = new LinkedHashMap<>();
        Map<String, Integer> paymentKeys = new LinkedHashMap<>();
        Map<String, Integer> statusKeys = new LinkedHashMap<>();

        List<CustomerDimension> customers = new ArrayList<>();
        List<ProductDimension> products = new ArrayList<>();
        List<DateDimension> dates = new ArrayList<>();
        List<CityDimension> cities = new ArrayList<>();
        List<PaymentDimension> payments = new ArrayList<>();
        List<StatusDimension> statuses = new ArrayList<>();
        List<SalesFact> salesFacts = new ArrayList<>();

        int nextCustomerKey = 1;
        int nextProductKey = 1;
        int nextDateKey = 1;
        int nextCityKey = 1;
        int nextPaymentKey = 1;
        int nextStatusKey = 1;

        for (RetailRecord record : records) {
            Integer customerKey = customerKeys.get(record.getCustomer());
            if (customerKey == null) {
                customerKey = nextCustomerKey++;
                customerKeys.put(record.getCustomer(), customerKey);
                customers.add(new CustomerDimension(customerKey, record.getCustomer()));
            }

            String productNaturalKey = record.getProduct() + "|" + record.getCategory();
            Integer productKey = productKeys.get(productNaturalKey);
            if (productKey == null) {
                productKey = nextProductKey++;
                productKeys.put(productNaturalKey, productKey);
                products.add(new ProductDimension(productKey, record.getProduct(), record.getCategory()));
            }

            Integer dateKey = dateKeys.get(record.getOrderDate());
            if (dateKey == null) {
                dateKey = nextDateKey++;
                dateKeys.put(record.getOrderDate(), dateKey);
                LocalDate orderDate = LocalDate.parse(record.getOrderDate());
                dates.add(new DateDimension(
                        dateKey,
                        record.getOrderDate(),
                        orderDate.getYear(),
                        orderDate.getMonthValue(),
                        ((orderDate.getMonthValue() - 1) / 3) + 1,
                        orderDate.getDayOfWeek().toString()));
            }

            Integer cityKey = cityKeys.get(record.getCity());
            if (cityKey == null) {
                cityKey = nextCityKey++;
                cityKeys.put(record.getCity(), cityKey);
                cities.add(new CityDimension(cityKey, record.getCity()));
            }

            Integer paymentKey = paymentKeys.get(record.getPaymentMethod());
            if (paymentKey == null) {
                paymentKey = nextPaymentKey++;
                paymentKeys.put(record.getPaymentMethod(), paymentKey);
                payments.add(new PaymentDimension(paymentKey, record.getPaymentMethod()));
            }

            Integer statusKey = statusKeys.get(record.getOrderStatus());
            if (statusKey == null) {
                statusKey = nextStatusKey++;
                statusKeys.put(record.getOrderStatus(), statusKey);
                statuses.add(new StatusDimension(statusKey, record.getOrderStatus()));
            }

            salesFacts.add(new SalesFact(
                    record.getOrderId(),
                    dateKey,
                    customerKey,
                    productKey,
                    cityKey,
                    paymentKey,
                    statusKey,
                    record.getPrice(),
                    record.getQuantity(),
                    record.getDiscount(),
                    record.getSalesAmount()));
        }

        return new RetailWarehouse(customers, products, dates, cities, payments, statuses, salesFacts);
    }
}
