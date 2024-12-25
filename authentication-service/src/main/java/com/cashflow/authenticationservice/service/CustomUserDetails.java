package com.cashflow.authenticationservice.service;

import com.cashflow.authenticationservice.dto.AuthenticationUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final AuthenticationUserDto authenticationUserDto;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Stream.of(authenticationUserDto.getUserRole())
                .map(userRole -> new SimpleGrantedAuthority(userRole.name()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return authenticationUserDto.getPassword();
    }

    @Override
    public String getUsername() {
        return authenticationUserDto.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
