package com.retailproject.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableDataResponse {
    private List<String> columns;
    private List<List<String>> rows;
}
