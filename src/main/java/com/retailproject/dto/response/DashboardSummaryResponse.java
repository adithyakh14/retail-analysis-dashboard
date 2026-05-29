package com.retailproject.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardSummaryResponse {
    private Double totalSales;
    private Integer totalOrders;
    private Integer totalQuantity;
    private Double averageOrderValue;
    private Double averageDiscount;
    private Integer activeCustomers;
    private Integer totalProducts;
}
