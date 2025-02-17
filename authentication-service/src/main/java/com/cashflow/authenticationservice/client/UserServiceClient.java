package com.cashflow.authenticationservice.client;

import com.cashflow.authenticationservice.dto.UserDto;
import com.cashflow.authenticationservice.requests.UserRegistrationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", path = "/v1/user-service")
public interface UserServiceClient {

    @PostMapping("/create")
    void createUser(@RequestBody UserRegistrationRequest userRegistrationRequest);

    @GetMapping("/getUserByEmail/{email}")
    UserDto getUserDtoByEmail(@PathVariable String email);
}
