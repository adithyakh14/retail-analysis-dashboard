package com.retailproject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DateDimension {
    private final int dateKey;
    private final String orderDate;
    private final int year;
    private final int month;
    private final int quarter;
    private final String dayOfWeek;
}
