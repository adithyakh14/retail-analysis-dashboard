package com.retailproject;

public class DateDimension {
    private final int dateKey;
    private final String orderDate;
    private final int year;
    private final int month;
    private final int quarter;
    private final String dayOfWeek;

    public DateDimension(int dateKey, String orderDate, int year, int month, int quarter, String dayOfWeek) {
        this.dateKey = dateKey;
        this.orderDate = orderDate;
        this.year = year;
        this.month = month;
        this.quarter = quarter;
        this.dayOfWeek = dayOfWeek;
    }

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
