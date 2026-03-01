package com.example.ride_pricing.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("vehicles")
public class Vehicle {

    @Id
    private Long id;

    private String name;

    @Column("price_increment")
    private Double priceIncrement;

    private Boolean active;

    public Vehicle() {
    }

    public Vehicle(Long id, String name, Double priceIncrement, Boolean active) {
        this.id = id;
        this.name = name;
        this.priceIncrement = priceIncrement;
        this.active = active;
    }

    public Vehicle(Object o, String vehicleName) {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPriceIncrement() {
        return priceIncrement;
    }

    public void setPriceIncrement(Double priceIncrement) {
        this.priceIncrement = priceIncrement;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}