package com.cashflow.userservice.controller;

import com.cashflow.userservice.dto.AuthenticationUserDto;
import com.cashflow.userservice.dto.UserDto;
import com.cashflow.userservice.requests.UserRegistrationRequest;
import com.cashflow.userservice.requests.UserUpdateProfileRequest;
import com.cashflow.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v1/user-service")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Value("${internal.key}")
    private String internalKey;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public void createUser(@Valid @RequestBody UserRegistrationRequest userRegistrationRequest,
                           @RequestHeader("Internal-Key") String key) {
        if (!key.equals(internalKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access denied");
        }
        userService.createUser(userRegistrationRequest);
    }

    @GetMapping("/getUserByEmail/{email}")
    @ResponseStatus(HttpStatus.OK)
    public AuthenticationUserDto getAuthenticationUserByEmail(@PathVariable String email,
                                                              @RequestHeader("Internal-Key") String key) {
        if (!key.equals(internalKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access denied");
        }
        return userService.getAuthenticationUserByEmail(email);
    }

    @GetMapping("/view/{username}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto viewUserProfile(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }

    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@userService.findUserById(#userUpdateProfileRequest.id).email == principal")
    public void updateProfile(@Valid @RequestBody UserUpdateProfileRequest userUpdateProfileRequest) {
        userService.updateUserProfile(userUpdateProfileRequest);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_ADMIN') or @userService.findUserById(#id).email == principal")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
    }
}
