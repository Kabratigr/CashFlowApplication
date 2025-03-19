package com.cashflow.transactionservice.client;

import com.cashflow.transactionservice.dto.UserCustomerIdDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/v1/user-service")
public interface UserServiceClient {

    @GetMapping("/getCustomerId/{id}")
    UserCustomerIdDto getUserCustomerIdById(@PathVariable("id") Long id);
}
