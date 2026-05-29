package com.retailproject.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AskResponse {
    private String query;
    private List<String> suggestions;
    private Integer recordCount;
    private String answer;
    private String interpretation;
    private TableDataResponse table;
    private ChartResponse chart;
}
