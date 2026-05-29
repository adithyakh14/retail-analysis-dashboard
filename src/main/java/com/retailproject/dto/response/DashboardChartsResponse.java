package com.retailproject.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardChartsResponse {
    private List<ChartPointResponse> salesTrend;
    private List<ChartPointResponse> categoryPerformance;
    private List<ChartPointResponse> topProducts;
    private List<ChartPointResponse> bottomProducts;
    private List<ChartPointResponse> cityPerformance;
    private List<ChartPointResponse> topCustomers;
    private List<ChartPointResponse> paymentPerformance;
    private List<ChartPointResponse> discountImpact;
}
