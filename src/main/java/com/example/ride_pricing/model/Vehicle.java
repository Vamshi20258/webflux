package com.example.ride_pricing.model;

import jakarta.persistence.*;

@Entity
@Table(name="vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column(name="price_increment")
    private Double priceIncrement;

    private Boolean active = true;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

