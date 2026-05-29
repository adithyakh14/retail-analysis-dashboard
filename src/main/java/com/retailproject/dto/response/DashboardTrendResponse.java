package com.retailproject.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardTrendResponse {
    private String summary;
    private String focusedDate;
    private List<InsightCardResponse> highlights;
}
