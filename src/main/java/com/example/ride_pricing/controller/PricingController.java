package com.example.ride_pricing.controller;

import com.example.ride_pricing.common.ApiResponse;
import com.example.ride_pricing.model.*;
import com.example.ride_pricing.service.PricingService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/pricing")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @GetMapping("/calculate")
    public Mono<ApiResponse<Object>> getPrice(@RequestParam String vehicle,
                                              @RequestParam Double kms,
                                              @RequestParam String serviceType) {

        return pricingService.calculatePrice(vehicle, kms, serviceType)
                .map(response ->
                        ApiResponse.success(
                                200,
                                "Price calculated successfully",
                                response
                        )
                );
    }


    @PutMapping("/updatePrice")
    public Mono<ApiResponse<Object>> updatePrice(@RequestParam String vehicle,
                                                 @RequestParam Double kms,
                                                 @RequestParam String serviceType,
                                                 @RequestParam Double newPrice) {

        return pricingService.updatePrice(vehicle, kms, serviceType, newPrice)
                .map(response ->
                        ApiResponse.success(
                                200,
                                "Price updated successfully",
                                response
                        )
                );
    }





    @GetMapping("/updatedHistory")
    public Mono<ApiResponse<Object>> history() {

        return pricingService.getAllHistory()
                .collectList()
                .map(list ->
                        ApiResponse.success(
                                200,
                                "Successfully Retrieved Pricing History",
                                list
                        )
                );
    }

    @PostMapping("/addPrice")
    public Mono<ApiResponse<Object>> addSlab(@RequestBody AddSlabRequest request) {
        return pricingService.addSlab(request)
                .map(response ->
                        ApiResponse.success(201, "Slab created and pricing auto-generated successfully", response)
                );
    }
}