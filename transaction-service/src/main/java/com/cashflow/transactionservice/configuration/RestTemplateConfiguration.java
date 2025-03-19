package com.cashflow.transactionservice.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class RestTemplateConfiguration {

    @Value("${saltedge.app-id}")
    private String appId;

    @Value("${saltedge.secret}")
    private String appSecret;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(List.of((request, body, execution) -> {
            HttpHeaders headers = request.getHeaders();
            headers.add("Accept", "application/json");
            headers.add("Content-Type", "application/json");
            headers.add("App-id", appId);
            headers.add("Secret", appSecret);
            return execution.execute(request, body);
        }));
        return restTemplate;
    }
}
