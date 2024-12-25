package com.cashflow.authenticationservice.controller;

import com.cashflow.authenticationservice.dto.TokenDto;
import com.cashflow.authenticationservice.requests.UserLoginRequest;
import com.cashflow.authenticationservice.requests.UserRegistrationRequest;
import com.cashflow.authenticationservice.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/authentication-service")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    public void registration(@RequestBody UserRegistrationRequest userRegistrationRequest) {
        authenticationService.userRegistration(userRegistrationRequest);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public TokenDto login(@RequestBody UserLoginRequest userLoginRequest) {
        return authenticationService.userLogin(userLoginRequest);
    }
}
