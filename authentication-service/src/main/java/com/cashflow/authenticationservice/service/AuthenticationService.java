package com.cashflow.authenticationservice.service;

import com.cashflow.authenticationservice.client.UserServiceClient;
import com.cashflow.authenticationservice.dto.TokenDto;
import com.cashflow.authenticationservice.exceptions.WrongCredentialsException;
import com.cashflow.authenticationservice.requests.UserLoginRequest;
import com.cashflow.authenticationservice.requests.UserRegistrationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;

    private final UserServiceClient userServiceClient;

    private final JwtService jwtService;

    public void userRegistration(UserRegistrationRequest userRegistrationRequest) {
        userServiceClient.createUser(userRegistrationRequest);
    }

    public TokenDto userLogin(UserLoginRequest userLoginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginRequest.getEmail(), userLoginRequest.getPassword()));
        if (authentication.isAuthenticated()) {
            return TokenDto.builder()
                    .token(jwtService.generateToken(userLoginRequest.getEmail()))
                    .build();
        } else {
            throw new WrongCredentialsException("Wrong credentials");
        }
    }
}
