package com.retailproject;

public record DateDimension(
        int dateKey,
        String orderDate,
        int year,
        int month,
        int quarter,
        String dayOfWeek) {
    public int getDateKey() {
        return dateKey;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getQuarter() {
        return quarter;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }
}
