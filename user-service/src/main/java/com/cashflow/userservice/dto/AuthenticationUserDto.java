package com.cashflow.userservice.dto;

import com.cashflow.userservice.enums.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticationUserDto {
    private Long id;
    private UserRole userRole;
    private String email;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
}
