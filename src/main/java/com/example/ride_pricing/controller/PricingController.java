package com.example.ride_pricing.controller;

import com.example.ride_pricing.model.PricingHistory;
import com.example.ride_pricing.service.PricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pricing")
public class PricingController {

    @Autowired
    private PricingService pricingService;


    @GetMapping("/calculate")
    public Double getPrice(@RequestParam String vehicle,
                           @RequestParam Double kms,
                           @RequestParam String serviceType) {

        return pricingService.calculatePrice(vehicle, kms, serviceType);
    }


    @PutMapping("/updatePrice")
    public String updatePrice(@RequestParam String vehicle,
                              @RequestParam Double kms,
                              @RequestParam String serviceType,
                              @RequestParam Double newPrice) {

        return pricingService.updatePrice(vehicle, kms, serviceType, newPrice);
    }

    @GetMapping("/UpdatedHistory")
    public List<PricingHistory> history()
    {
        return pricingService.getAllHistory();
    }
}
