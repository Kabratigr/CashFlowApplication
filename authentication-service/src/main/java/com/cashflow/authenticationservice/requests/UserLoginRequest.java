package com.cashflow.authenticationservice.requests;

import lombok.Getter;

@Getter
public class UserLoginRequest {
    private String email;
    private String password;
}
