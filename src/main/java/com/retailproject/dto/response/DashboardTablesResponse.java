package com.retailproject.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardTablesResponse {
    private TableDataResponse categoryContribution;
    private TableDataResponse cityContribution;
}
