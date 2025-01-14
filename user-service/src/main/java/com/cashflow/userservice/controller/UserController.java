package com.cashflow.userservice.controller;

import com.cashflow.userservice.dto.AuthenticationUserDto;
import com.cashflow.userservice.dto.UserDto;
import com.cashflow.userservice.model.User;
import com.cashflow.userservice.requests.UserRegistrationRequest;
import com.cashflow.userservice.requests.UserUpdateProfileRequest;
import com.cashflow.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        userService.createUser(userRegistrationRequest);
    }

    @GetMapping("/getUserByEmail/{email}")
    @ResponseStatus(HttpStatus.OK)
    public AuthenticationUserDto getAuthenticationUserByEmail(@PathVariable String email,
                                                              @RequestHeader("Internal-Key") String key) {
        if (!key.equals(internalKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return userService.getAuthenticationUserByEmail(email);
    }

    @GetMapping("/view/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto viewUserProfile(@PathVariable Long id) {
        User user = userService.findUserById(id);
        if (!user.getId().equals(userService.getCurrentUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return userService.convertUserToUserDto(user);
    }

    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public void updateProfile(@Valid @RequestBody UserUpdateProfileRequest userUpdateProfileRequest) {
        User user = userService.findUserById(userUpdateProfileRequest.getId());
        if (!user.getId().equals(userService.getCurrentUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        userService.updateUserProfile(userUpdateProfileRequest, user);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteUser(@PathVariable Long id) {
        User user = userService.findUserById(id);
        if (!user.getId().equals(userService.getCurrentUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        userService.deleteUserById(user);
    }
}
