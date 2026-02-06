package com.example.moneytracker.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProperties {

    private final String secret;
    private final long expirationSeconds;

    public JwtProperties(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-seconds}") long expirationSeconds
    ) {
        this.secret = secret;
        this.expirationSeconds = expirationSeconds;
    }

    public String getSecret() {
        return secret;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }
}

