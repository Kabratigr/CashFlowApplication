package com.cashflow.userservice.service;

import com.cashflow.userservice.dto.AuthenticationUserDto;
import com.cashflow.userservice.dto.UserDto;
import com.cashflow.userservice.enums.UserRole;
import com.cashflow.userservice.enums.UserStatus;
import com.cashflow.userservice.exceptions.UserNotFoundException;
import com.cashflow.userservice.model.User;
import com.cashflow.userservice.repository.UserRepository;
import com.cashflow.userservice.requests.UserRegistrationRequest;
import com.cashflow.userservice.requests.UserUpdateProfileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public void createUser(UserRegistrationRequest userRegistrationRequest) {
        User userToCreate = User.builder()
                .role(UserRole.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .email(userRegistrationRequest.getEmail())
                .username(userRegistrationRequest.getUsername())
                .password(passwordEncoder.encode(userRegistrationRequest.getPassword()))
                .firstName(userRegistrationRequest.getFirstName())
                .lastName(userRegistrationRequest.getLastName())
                .build();
        userRepository.save(userToCreate);
    }

    public UserDto convertUserToUserDto(User userEntity) {
        return UserDto.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsername())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .build();
    }

    public AuthenticationUserDto getAuthenticationUserByEmail(String email) {
        return convertUserToAuthenticationUserDto(userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User with email \"%s\" not found", email))));
    }

    protected AuthenticationUserDto convertUserToAuthenticationUserDto(User userEntity) {
        return AuthenticationUserDto.builder()
                .id(userEntity.getId())
                .userRole(userEntity.getRole())
                .email(userEntity.getEmail())
                .username(userEntity.getUsername())
                .password(userEntity.getPassword())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .build();
    }

    public void updateUserProfile(UserUpdateProfileRequest userUpdateProfileRequest, User userToUpdate) {
        userToUpdate.setEmail(Optional.ofNullable(userUpdateProfileRequest.getEmail()).orElse(userToUpdate.getEmail()));
        userToUpdate.setUsername(Optional.ofNullable(userUpdateProfileRequest.getUsername()).orElse(userToUpdate.getUsername()));
        userToUpdate.setPassword(userUpdateProfileRequest.getPassword() != null ? passwordEncoder.encode(userUpdateProfileRequest.getPassword()) : userToUpdate.getPassword());
        userToUpdate.setFirstName(Optional.ofNullable(userUpdateProfileRequest.getFirstName()).orElse(userToUpdate.getFirstName()));
        userToUpdate.setLastName(Optional.ofNullable(userUpdateProfileRequest.getLastName()).orElse(userToUpdate.getLastName()));
        userRepository.save(userToUpdate);
    }

    public void deleteUserById(User userToDelete) {
        userRepository.delete(userToDelete);
    }

    public User findUserById(Long id) {
        return userRepository.findUserById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User with id \"%s\" not found", id)));
    }

    public Long getCurrentUserId() {
        return Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
