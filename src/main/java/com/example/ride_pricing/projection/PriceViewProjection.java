package com.example.ride_pricing.projection;

public interface PriceViewProjection {

    String getVehicle();
    String getServiceType();
    String getRange();
    Double getPrice();
}