package com.cashflow.transactionservice.client;

import com.cashflow.transactionservice.dto.UserCustomerIdDto;
import com.cashflow.transactionservice.requests.UserUpdateCustomerIdRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", path = "/v1/user-service")
public interface UserServiceClient {

    @PutMapping("/updateCustomerId/{id}")
    void updateCustomerId(@PathVariable("id") Long id, @RequestBody UserUpdateCustomerIdRequest userUpdateCustomerIdRequest);

    @GetMapping("/getCustomerId/{id}")
    UserCustomerIdDto getUserCustomerIdById(@PathVariable("id") Long id);
}
