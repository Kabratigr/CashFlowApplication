package com.cashflow.authenticationservice.service;

import com.cashflow.authenticationservice.enums.UserRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    public String generateToken(Long userId, UserRole userRole) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userRole", userRole);
        return Jwts.builder()
                .claims()
                .add(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .and()
                .signWith(generateSignKey())
                .compact();
    }

    private SecretKey generateSignKey() {
        byte[] secretBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(secretBytes);
    }

}
