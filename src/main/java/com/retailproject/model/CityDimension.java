package com.retailproject.model;

public record CityDimension(int cityKey, String cityName) {
    public int getCityKey() {
        return cityKey;
    }

    public String getCityName() {
        return cityName;
    }
}
