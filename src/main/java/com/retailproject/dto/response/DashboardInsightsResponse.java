package com.retailproject.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardInsightsResponse {
    private List<InsightCardResponse> topInsights;
    private List<InsightCardResponse> watchouts;
}
