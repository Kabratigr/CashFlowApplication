package com.cashflow.authenticationservice.dto;

import com.cashflow.authenticationservice.enums.UserRole;
import lombok.Data;

@Data
public class AuthenticationUserDto {
    private Long id;
    private UserRole userRole;
    private String email;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
}
