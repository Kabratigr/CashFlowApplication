package com.cashflow.userservice.service;

import com.cashflow.userservice.dto.UserCustomerIdDto;
import com.cashflow.userservice.dto.UserDto;
import com.cashflow.userservice.dto.UserProfileDto;
import com.cashflow.userservice.enums.UserRole;
import com.cashflow.userservice.enums.UserStatus;
import com.cashflow.userservice.exceptions.UserNotFoundException;
import com.cashflow.userservice.model.User;
import com.cashflow.userservice.kafka.UserServiceProducer;
import com.cashflow.userservice.repository.UserRepository;
import com.cashflow.userservice.requests.UserRegistrationRequest;
import com.cashflow.userservice.dto.UserModificationKafkaDto;
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

    private final UserServiceProducer userServiceProducer;

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

    public UserProfileDto convertUserToUserProfileDto(User userEntity) {
        return UserProfileDto.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsername())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .build();
    }

    public UserDto getUserDtoByEmail(String email) {
        return convertUserToUserDto(userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User with email \"%s\" not found", email))));
    }

    protected UserDto convertUserToUserDto(User userEntity) {
        return UserDto.builder()
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

    public void updateCustomerId (UserModificationKafkaDto userModificationKafkaDto) {
        User userToUpdate = userRepository.findUserById(userModificationKafkaDto.getUserId())
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User with id \"%s\" not found", userModificationKafkaDto.getUserId())));
        userToUpdate.setCustomerId(userModificationKafkaDto.getCustomerId());
        userRepository.save(userToUpdate);
    }

    public UserCustomerIdDto getUserCustomerIdDtoById(Long id) {
        return convertUserToUserCustomerIdDto(userRepository.findUserById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User with id \"%s\" not found", id))));
    }

    protected UserCustomerIdDto convertUserToUserCustomerIdDto(User userEntity) {
        return UserCustomerIdDto.builder()
                .customerId(userEntity.getCustomerId())
                .build();
    }

    public void deleteUserById(User userToDelete) {
        UserModificationKafkaDto userModificationKafkaDto = UserModificationKafkaDto.builder()
                .userId(userToDelete.getId())
                .customerId(userToDelete.getCustomerId())
                .build();
        userServiceProducer.sendUserDeletionEvent(userModificationKafkaDto);
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
