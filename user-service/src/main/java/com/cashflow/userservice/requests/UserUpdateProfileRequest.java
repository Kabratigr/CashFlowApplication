package com.cashflow.userservice.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserUpdateProfileRequest {
    @NotNull(message = "Id is required")
    private Long id;
    @Email(message = "Email must be valid")
    private String email;
    @Pattern(regexp = "^(?=[a-zA-Z0-9._]{8,20}$)(?!.*[_.]{2})[^_.].*[^_.]$",
            message = "Username must be 8-20 characters long can only contain letters, digits, periods, and underscores")
    private String username;
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "Password must be at least 8 characters, containing 1 letter and 1 number")
    private String password;
    private String firstName;
    private String lastName;
}
