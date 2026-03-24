package com.nativeflow.backend.common.security;

public record ClientRequestMetadata(
        String deviceName,
        String ipAddress,
        String userAgent
) {
}
