package com.retailproject.dto;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AskRequest extends DashboardFilterRequest {
    private String q = "";

    public String resolvedQuestion() {
        return safe(q);
    }

    public Map<String, String> toQueryMap() {
        Map<String, String> params = toFilterMap();
        params.put("q", resolvedQuestion());
        return params;
    }
}
