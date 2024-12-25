package com.cashflow.apigateway.filter;

import com.cashflow.apigateway.jwt.JwtValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GatewayFilter {

    private final JwtValidator jwtValidator;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest httpRequest = exchange.getRequest();

        List<String> allowedEndpoints = List.of(
                "/v1/authentication-service/registration",
                "/v1/authentication-service/login",
                "/v1/user-service/getUserByEmail/",
                "/v1/user-service/create",
                "/eureka"
        );

        Predicate<ServerHttpRequest> isEndpointSecured = request -> allowedEndpoints.stream()
                .noneMatch(uri -> request.getURI().getPath().startsWith(uri));

        if (isEndpointSecured.test(httpRequest)) {

            if (!httpRequest.getHeaders().containsKey("Authorization")) {
                return unauthorizedResponse(exchange);
            }

            String token = httpRequest.getHeaders().getOrEmpty("Authorization").getFirst();

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            try {
                jwtValidator.validateToken(token);
            } catch (Exception e) {
                return unauthorizedResponse(exchange);
            }
        }
        return chain.filter(exchange);
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange) {
        ServerHttpResponse httpResponse = exchange.getResponse();
        httpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
        return httpResponse.setComplete();
    }
}
