package com.retailproject.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChartResponse {
    private String title;
    private String type;
    private String format;
    private List<ChartPointResponse> points;
}
