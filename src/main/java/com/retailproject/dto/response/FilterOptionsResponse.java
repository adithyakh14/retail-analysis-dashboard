package com.retailproject.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilterOptionsResponse {
    private List<String> dates;
    private List<String> cities;
    private List<String> categories;
    private List<String> products;
    private List<String> paymentMethods;
    private List<String> statuses;
    private List<String> customers;
}
