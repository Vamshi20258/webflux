package com.example.ride_pricing.model;

public class PriceViewResponse {
    private String vehicle;
    private String serviceType;
    private String range;
    private Double price;
    private String status;

    public PriceViewResponse(String vehicle, String serviceType, String range, Double price, String status) {
        this.vehicle = vehicle;
        this.serviceType = serviceType;
        this.range = range;
        this.price = price;
        this.status = status;
    }

    public PriceViewResponse() {
    }

    public PriceViewResponse(String name, String name1, String s, Double finalPrice) {
    }


    // GETTERS ARE REQUIRED FOR JSON SERIALIZATION
    public String getVehicle() { return vehicle; }
    public String getServiceType() { return serviceType; }
    public String getRange() { return range; }
    public Double getPrice() { return price; }
    public String getStatus() { return status; }
}