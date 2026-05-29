package com.retailproject.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardMetaResponse {
    private String refreshedAt;
    private Integer overallRecordCount;
    private Integer filteredRecordCount;
}
