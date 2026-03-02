package com.example.ride_pricing.model;

import java.time.LocalDateTime;

public class PriceViewResponse {

    private String vehicle;
    private String serviceType;
    private String range;
    private Double price;
    private String status;

    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    public PriceViewResponse() {}

    // FULL constructor
    public PriceViewResponse(String vehicle,
                             String serviceType,
                             String range,
                             Double price,
                             String status,
                             LocalDateTime createdAt,
                             String createdBy,
                             LocalDateTime updatedAt,
                             String updatedBy) {
        this.vehicle = vehicle;
        this.serviceType = serviceType;
        this.range = range;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    // SIMPLIFIED constructor (used in calculate + active methods)
    public PriceViewResponse(String vehicle,
                             String serviceType,
                             String range,
                             Double price,
                             String status,
                             LocalDateTime createdAt) {
        this.vehicle = vehicle;
        this.serviceType = serviceType;
        this.range = range;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
    }

    public PriceViewResponse(String name, String name1, String range, Long finalPrice, String status, LocalDateTime createdAt, String createdBy, LocalDateTime updatedAt, String updatedBy) {
    }

    public String getVehicle() { return vehicle; }
    public String getServiceType() { return serviceType; }
    public String getRange() { return range; }
    public Double getPrice() { return price; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
}