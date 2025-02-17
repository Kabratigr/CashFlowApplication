package com.cashflow.transactionservice.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

    @Value("${saltedge.app-id}")
    private String appId;

    @Value("${saltedge.secret}")
    private String appSecret;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://www.saltedge.com/api/v5")
                .defaultHeaders(headers -> {
                        headers.add("Accept", "application/json");
                        headers.add("Content-type", "application/json");
                        headers.add("App-id", appId);
                        headers.add("Secret", appSecret);
                })
                .build();
    }
}
