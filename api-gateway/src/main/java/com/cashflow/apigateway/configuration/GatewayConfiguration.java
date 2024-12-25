package com.cashflow.apigateway.configuration;

import com.cashflow.apigateway.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", route -> route
                        .path("/v1/user-service/**")
                        .filters(filter -> filter.filter(jwtAuthenticationFilter))
                        .uri("lb://USER-SERVICE"))

                .route("authentication-service", route -> route
                        .path("/v1/authentication-service/**")
                        .uri("lb://AUTHENTICATION-SERVICE"))

                .build();
    }
}
