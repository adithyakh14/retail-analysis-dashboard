package com.retailproject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StatusDimension {
    private final int statusKey;
    private final String orderStatus;
}
