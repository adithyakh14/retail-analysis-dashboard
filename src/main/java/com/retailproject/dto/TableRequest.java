package com.retailproject.dto;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableRequest extends DashboardFilterRequest {
    private String name = "raw-input";
    private Integer page = 1;
    private Integer pageSize = 10;
    private String search = "";

    public String resolvedName() {
        return safe(name).isBlank() ? "raw-input" : name;
    }

    public Map<String, String> toQueryMap() {
        Map<String, String> params = toFilterMap();
        params.put("name", resolvedName());
        params.put("page", String.valueOf(page == null ? 1 : page));
        params.put("pageSize", String.valueOf(pageSize == null ? 10 : pageSize));
        params.put("search", safe(search));
        return params;
    }
}
