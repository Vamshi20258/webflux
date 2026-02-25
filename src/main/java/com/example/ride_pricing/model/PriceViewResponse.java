package com.example.ride_pricing.model;

public class PriceViewResponse {

    private String vehicle;
    private String serviceType;
    private String range;     // Slab range
    private Double price;     // Final price

    public PriceViewResponse(String vehicle,
                             String serviceType,
                             String range,
                             Double price) {
        this.vehicle = vehicle;
        this.serviceType = serviceType;
        this.range = range;
        this.price = price;
    }

    public String getVehicle() {
        return vehicle;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getRange() {
        return range;
    }

    public Double getPrice() {
        return price;
    }
}