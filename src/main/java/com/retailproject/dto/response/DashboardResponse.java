package com.retailproject.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardResponse {
    private DashboardSummaryResponse summary;
    private DashboardMetaResponse meta;
    private DashboardChartsResponse charts;
    private DashboardTrendResponse trend;
    private DashboardInsightsResponse insights;
    private DashboardTablesResponse tables;
}
