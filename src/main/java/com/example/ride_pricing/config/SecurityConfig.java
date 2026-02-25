package com.example.ride_pricing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final SecurityErrorHandler securityErrorHandler;

    public SecurityConfig(SecurityErrorHandler securityErrorHandler) {
        this.securityErrorHandler = securityErrorHandler;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/pricing/calculate").hasAnyRole("USER", "ADMIN")
                        .pathMatchers("/pricing/updatedHistory").hasAnyRole("USER", "ADMIN")
                        .pathMatchers("/pricing/addPrice").hasRole("ADMIN")
                        .pathMatchers("/pricing/updatePrice").hasRole("ADMIN")
                        .pathMatchers("/pricing/add-slab-price").hasAnyRole("USER","ADMIN")
                        .anyExchange().authenticated()
                )

                .oauth2ResourceServer(oauth ->
                        oauth.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(new KeycloakRoleConverter())
                        )
                )

                .exceptionHandling(e ->
                        e.authenticationEntryPoint(securityErrorHandler)
                );

        return http.build();
    }
}