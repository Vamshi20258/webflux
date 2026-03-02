package com.example.ride_pricing.controller;

import com.example.ride_pricing.common.ApiResponse;
import com.example.ride_pricing.model.*;
import com.example.ride_pricing.service.PricingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pricing")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }



    @GetMapping("/calculate")
    public Mono<ResponseEntity<ApiResponse<PriceViewResponse>>> getPrice(
            @RequestParam String vehicle,
            @RequestParam Double kms,
            @RequestParam String serviceType) {

        return pricingService.calculatePrice(vehicle, kms, serviceType)
                .map(response ->
                        ResponseEntity.ok(
                                ApiResponse.success(200,
                                        "Price calculated successfully",
                                        response)))
                .onErrorResume(RuntimeException.class, ex ->
                        Mono.just(
                                ResponseEntity.badRequest()
                                        .body(ApiResponse.failed(400, ex.getMessage()))
                        ));
    }


    @PostMapping("/addPrice")
    public Mono<ResponseEntity<ApiResponse<Object>>> addPrice(
            @RequestBody SlabPriceRequest request) {

        return pricingService.addNewPricing(request)
                .map(api -> ResponseEntity.status(api.getStatus()).body(api))
                .onErrorResume(RuntimeException.class, ex ->
                        Mono.just(
                                ResponseEntity.badRequest()
                                        .body(ApiResponse.failed(400, ex.getMessage()))
                        ));
    }



    @PutMapping("/updatePrice")
    public Mono<ResponseEntity<ApiResponse<PriceUpdateResponse>>> updatePrice(
            @RequestParam String vehicle,
            @RequestParam Double kms,
            @RequestParam String serviceType,
            @RequestParam Double newPrice) {

        return pricingService.updatePrice(vehicle, kms, serviceType, newPrice)
                .map(response ->
                        ResponseEntity.ok(
                                ApiResponse.success(200,
                                        "Price updated successfully",
                                        response)))
                .onErrorResume(RuntimeException.class, ex ->
                        Mono.just(
                                ResponseEntity.badRequest()
                                        .body(ApiResponse.failed(400, ex.getMessage()))
                        ));
    }



    @GetMapping("/updatedHistory")
    public Mono<ResponseEntity<ApiResponse<Object>>> history() {

        return pricingService.getAllHistory()
                .collectList()
                .map(list ->
                        ResponseEntity.ok(
                                ApiResponse.success(200,
                                        "Successfully Retrieved Pricing History",
                                        list)));
    }



    @GetMapping("/all")
    public Mono<ResponseEntity<ApiResponse<Object>>> getAll() {

        return pricingService.getAllPrices()
                .collectList()
                .map(list -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("pricingDetails", list);
                    return ResponseEntity.ok(
                            ApiResponse.success(200,
                                    "Successfully Retrieved All Pricing Details",
                                    data));
                });
    }

    @GetMapping("/active-prices")
    public Mono<ResponseEntity<ApiResponse<Object>>> getActivePrices(
            @RequestParam Double kms,
            @RequestParam String serviceType) {

        return pricingService.getActivePricesByKmsAndService(kms, serviceType)
                .map(ResponseEntity::ok)
                .onErrorResume(ex ->
                        Mono.just(ResponseEntity.badRequest().body(
                                ApiResponse.failed(400, ex.getMessage())
                        ))
                );
    }

    @PostMapping("/add-vehicle")
    public Mono<ResponseEntity<ApiResponse<Object>>> addVehicle(
            @RequestParam String name) {

        return pricingService.addVehicle(name)
                .map(response -> {

                    if (response.getStatus() == 409) {
                        return ResponseEntity.status(409).body(response);
                    }

                    return ResponseEntity.status(201).body(response);
                })
                .onErrorResume(ex ->
                        Mono.just(ResponseEntity.badRequest().body(
                                ApiResponse.failed(400, ex.getMessage())
                        ))
                );
    }
}