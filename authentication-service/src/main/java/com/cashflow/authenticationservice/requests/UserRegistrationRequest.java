package com.cashflow.authenticationservice.requests;

import lombok.Getter;

@Getter
public class UserRegistrationRequest {
    private String email;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
}
