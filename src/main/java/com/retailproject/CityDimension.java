package com.retailproject;

public class CityDimension {
    private final int cityKey;
    private final String cityName;

    public CityDimension(int cityKey, String cityName) {
        this.cityKey = cityKey;
        this.cityName = cityName;
    }

    public int getCityKey() {
        return cityKey;
    }

    public String getCityName() {
        return cityName;
    }
}
