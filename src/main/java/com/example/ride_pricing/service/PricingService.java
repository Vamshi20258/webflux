package com.example.ride_pricing.service;

import com.example.ride_pricing.common.ApiResponse;
import com.example.ride_pricing.model.*;
import com.example.ride_pricing.repository.*;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class PricingService {

    private final VehicleRepository vehicleRepo;
    private final ServiceTypeRepository serviceRepo;
    private final PricingSlabRepository slabRepo;
    private final VehiclePricingRepository vpRepo;
    private final PricingHistoryRepository historyRepo;
    private final DatabaseClient databaseClient;

    public PricingService(
            VehicleRepository vehicleRepo,
            ServiceTypeRepository serviceRepo,
            PricingSlabRepository slabRepo,
            VehiclePricingRepository vpRepo,
            PricingHistoryRepository historyRepo,
            DatabaseClient databaseClient
    ) {
        this.vehicleRepo = vehicleRepo;
        this.serviceRepo = serviceRepo;
        this.slabRepo = slabRepo;
        this.vpRepo = vpRepo;
        this.historyRepo = historyRepo;
        this.databaseClient = databaseClient;
    }



    public Mono<PriceViewResponse> calculatePrice(String vehicleName,
                                                  Double kms,
                                                  String serviceName) {

        return vehicleRepo.findByNameIgnoreCase(vehicleName)
                .switchIfEmpty(Mono.error(new RuntimeException("Vehicle not found")))
                .zipWith(serviceRepo.findByNameIgnoreCase(serviceName)
                        .switchIfEmpty(Mono.error(new RuntimeException("Service not found"))))
                .flatMap(tuple -> {

                    Vehicle vehicle = tuple.getT1();
                    ServiceType service = tuple.getT2();

                    return slabRepo.findMatchingSlab(kms)
                            .next()
                            .switchIfEmpty(Mono.error(new RuntimeException("KMs not supported")))
                            .flatMap(slab ->
                                    vpRepo.findByVehicleIdAndSlabIdAndServiceId(
                                                    vehicle.getId(),
                                                    slab.getId(),
                                                    service.getId()
                                            )
                                            .switchIfEmpty(Mono.error(new RuntimeException("Pricing not configured")))
                                            .map(vp -> new PriceViewResponse(
                                                    vehicle.getName(),
                                                    service.getName(),
                                                    slab.getMinKm() + " - " + slab.getMaxKm() + " KM",
                                                    vp.getFinalPrice(),
                                                    Boolean.TRUE.equals(slab.getActive()) ? "ACTIVE" : "EXPIRED",
                                                    vp.getCreatedAt()
                                            ))
                            );
                });
    }



    public Mono<ApiResponse<Object>> addNewPricing(SlabPriceRequest request) {

        return vehicleRepo.findByNameIgnoreCase(request.getVehicle())
                .switchIfEmpty(Mono.error(new RuntimeException("Vehicle not found")))
                .zipWith(serviceRepo.findByNameIgnoreCase(request.getServiceType())
                        .switchIfEmpty(Mono.error(new RuntimeException("Service not found"))))
                .flatMap(tuple -> {

                    Vehicle vehicle = tuple.getT1();
                    ServiceType service = tuple.getT2();

                    return slabRepo.findExactSlab(request.getMinKm(), request.getMaxKm())
                            .switchIfEmpty(slabRepo.save(createNewSlab(request)))
                            .flatMap(slab ->

                                    vpRepo.existsByVehicleIdAndSlabIdAndServiceId(
                                                    vehicle.getId(),
                                                    slab.getId(),
                                                    service.getId())
                                            .flatMap(exists -> {

                                                if (exists) {
                                                    return Mono.just(ApiResponse.failed(
                                                            409,
                                                            "Pricing already exists"
                                                    ));
                                                }

                                                VehiclePricing vp = new VehiclePricing();
                                                vp.setVehicleId(vehicle.getId());
                                                vp.setServiceId(service.getId());
                                                vp.setSlabId(slab.getId());
                                                vp.setFinalPrice(request.getPrice());
                                                vp.setCreatedAt(LocalDateTime.now());
                                                vp.setUpdatedAt(LocalDateTime.now());

                                                return vpRepo.save(vp)
                                                        .map(saved -> {

                                                            PriceViewResponse response =
                                                                    new PriceViewResponse(
                                                                            vehicle.getName(),
                                                                            service.getName(),
                                                                            slab.getMinKm() + " - " + slab.getMaxKm() + " KM",
                                                                            saved.getFinalPrice(),
                                                                            "ACTIVE",
                                                                            saved.getCreatedAt()
                                                                    );

                                                            return ApiResponse.success(
                                                                    201,
                                                                    "Pricing created successfully",
                                                                    response
                                                            );
                                                        });
                                            })
                            );
                });
    }

    private PricingSlab createNewSlab(SlabPriceRequest request) {

        PricingSlab slab = new PricingSlab();
        slab.setMinKm(request.getMinKm());
        slab.setMaxKm(request.getMaxKm());
        slab.setBasePrice(request.getPrice());
        slab.setActive(true);
        slab.setCreatedAt(LocalDateTime.now());
        slab.setUpdatedAt(LocalDateTime.now());

        return slab;
    }


    public Mono<PriceUpdateResponse> updatePrice(String vehicleName,
                                                 Double kms,
                                                 String serviceName,
                                                 Double newPrice) {

        return vehicleRepo.findByNameIgnoreCase(vehicleName)
                .switchIfEmpty(Mono.error(new RuntimeException("Vehicle not found")))
                .zipWith(serviceRepo.findByNameIgnoreCase(serviceName)
                        .switchIfEmpty(Mono.error(new RuntimeException("Service not found"))))
                .flatMap(tuple -> {

                    Vehicle vehicle = tuple.getT1();
                    ServiceType service = tuple.getT2();

                    return slabRepo.findMatchingSlab(kms)
                            .next()
                            .switchIfEmpty(Mono.error(new RuntimeException("kms out of range")))
                            .flatMap(slab ->
                                    vpRepo.findByVehicleIdAndSlabIdAndServiceId(
                                                    vehicle.getId(),
                                                    slab.getId(),
                                                    service.getId())
                                            .switchIfEmpty(Mono.error(new RuntimeException("Pricing not found")))
                                            .flatMap(vp -> {

                                                Double oldPrice = vp.getFinalPrice();

                                                PricingHistory history = new PricingHistory();
                                                history.setVehicleId(vehicle.getId());
                                                history.setSlabId(slab.getId());
                                                history.setServiceId(service.getId());
                                                history.setOldPrice(oldPrice);
                                                history.setNewPrice(newPrice);
                                                history.setChangedAt(LocalDateTime.now());

                                                vp.setFinalPrice(newPrice);
                                                vp.setUpdatedAt(LocalDateTime.now());

                                                return historyRepo.save(history)
                                                        .then(vpRepo.save(vp))
                                                        .thenReturn(new PriceUpdateResponse(
                                                                vehicle.getName(),
                                                                service.getName(),
                                                                slab.getMinKm(),
                                                                slab.getMaxKm(),
                                                                oldPrice,
                                                                newPrice,
                                                                vp.getUpdatedAt()
                                                        ));
                                            })
                            );
                });
    }



    public Flux<PriceViewResponse> getAllPrices() {

        String sql = """
            SELECT 
                v.name AS vehicle,
                s.name AS service_type,
                CONCAT(ps.min_km, ' - ', ps.max_km, ' KM') AS range,
                vp.final_price AS price,
                ps.active AS active,
                vp.created_at AS created_at
            FROM vehicle_pricing vp
            JOIN vehicles v ON v.id = vp.vehicle_id
            JOIN services s ON s.id = vp.service_id
            JOIN pricing_slabs ps ON ps.id = vp.slab_id
        """;

        return databaseClient.sql(sql)
                .map((row, metadata) -> {

                    Boolean active = row.get("active", Boolean.class);

                    String status = Boolean.TRUE.equals(active)
                            ? "ACTIVE"
                            : "EXPIRED";

                    return new PriceViewResponse(
                            row.get("vehicle", String.class),
                            row.get("service_type", String.class),
                            row.get("range", String.class),
                            row.get("price", Double.class),
                            status,
                            row.get("created_at", LocalDateTime.class)
                    );
                })
                .all();
    }



    public Flux<PricingHistory> getAllHistory() {
        return historyRepo.findAllByOrderByChangedAtDesc();
    }

    public Mono<ApiResponse<Object>> getActivePricesByKmsAndService(
            Double kms,
            String serviceName) {

        return serviceRepo.findByNameIgnoreCase(serviceName)
                .switchIfEmpty(Mono.error(new RuntimeException("Service not found")))
                .flatMap(service ->

                        slabRepo.findMatchingSlab(kms)
                                .next()
                                .switchIfEmpty(Mono.error(
                                        new RuntimeException("KMs out of supported range")))
                                .flatMap(slab -> {

                                    if (!Boolean.TRUE.equals(slab.getActive())) {
                                        return Mono.error(
                                                new RuntimeException("This slab is expired"));
                                    }

                                    String sql = """
                                    SELECT 
                                        v.name AS vehicle,
                                        s.name AS service_type,
                                        CONCAT(ps.min_km, ' - ', ps.max_km, ' KM') AS range,
                                        vp.final_price AS price,
                                        vp.created_at AS created_at
                                    FROM vehicle_pricing vp
                                    JOIN vehicles v ON v.id = vp.vehicle_id
                                    JOIN services s ON s.id = vp.service_id
                                    JOIN pricing_slabs ps ON ps.id = vp.slab_id
                                    WHERE vp.service_id = :serviceId
                                      AND vp.slab_id = :slabId
                                      AND ps.active = true
                                """;

                                    return databaseClient.sql(sql)
                                            .bind("serviceId", service.getId())
                                            .bind("slabId", slab.getId())
                                            .map((row, meta) ->
                                                    new PriceViewResponse(
                                                            row.get("vehicle", String.class),
                                                            row.get("service_type", String.class),
                                                            row.get("range", String.class),
                                                            row.get("price", Double.class),
                                                            "ACTIVE",
                                                            row.get("created_at", LocalDateTime.class)
                                                    )
                                            )
                                            .all()
                                            .collectList()
                                            .flatMap(list -> {

                                                if (list.isEmpty()) {
                                                    return Mono.error(new RuntimeException(
                                                            "No active pricing available for this range"));
                                                }

                                                return Mono.just(
                                                        ApiResponse.success(
                                                                200,
                                                                "Active pricing retrieved successfully",
                                                                list
                                                        )
                                                );
                                            });
                                })
                );
    }

    public Mono<ApiResponse<Object>> addVehicle(String vehicleName) {

        return vehicleRepo.findByNameIgnoreCase(vehicleName)
                .flatMap(existing ->
                        Mono.just(ApiResponse.failed(
                                409,
                                "Vehicle already available"
                        ))
                )
                .switchIfEmpty(
                        vehicleRepo.save(new Vehicle(null, vehicleName))
                                .map(saved ->
                                        ApiResponse.success(
                                                201,
                                                "Vehicle added successfully",
                                                saved.getName()
                                        )
                                )
                );
    }
}