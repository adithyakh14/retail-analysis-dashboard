package com.retailproject.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChartPointResponse {
    private String label;
    private Double value;
    private String filterKey;
    private String filterValue;
}
