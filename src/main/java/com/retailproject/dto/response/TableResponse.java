package com.retailproject.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableResponse {
    private String name;
    private String label;
    private List<String> columns;
    private List<List<String>> rows;
    private Integer page;
    private Integer pageSize;
    private Integer totalRows;
    private Integer totalPages;
    private String downloadUrl;
}
