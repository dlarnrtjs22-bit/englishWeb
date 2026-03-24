package com.nativeflow.backend.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth.jwt")
public record JwtProperties(
        String secret,
        String issuer,
        long accessTokenValiditySeconds,
        long refreshTokenValiditySeconds
) {
}
