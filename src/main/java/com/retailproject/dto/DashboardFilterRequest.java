package com.retailproject.dto;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardFilterRequest {
    private String dateFrom = "";
    private String dateTo = "";
    private String city = "";
    private String category = "";
    private String product = "";
    private String paymentMethod = "";
    private String status = "";
    private String customer = "";

    public Map<String, String> toFilterMap() {
        Map<String, String> filters = new LinkedHashMap<>();
        filters.put("dateFrom", safe(dateFrom));
        filters.put("dateTo", safe(dateTo));
        filters.put("city", safe(city));
        filters.put("category", safe(category));
        filters.put("product", safe(product));
        filters.put("paymentMethod", safe(paymentMethod));
        filters.put("status", safe(status));
        filters.put("customer", safe(customer));
        return filters;
    }

    protected String safe(String value) {
        return value == null ? "" : value;
    }
}
