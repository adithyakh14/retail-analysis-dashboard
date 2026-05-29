package com.retailproject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CityDimension {
    private final int cityKey;
    private final String cityName;
}
