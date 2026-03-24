package com.nativeflow.backend.common.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class ClientRequestMetadataResolver {

    public ClientRequestMetadata resolve(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String deviceName = userAgent == null || userAgent.isBlank()
                ? "unknown-device"
                : userAgent.substring(0, Math.min(userAgent.length(), 100));
        String forwardedFor = request.getHeader("X-Forwarded-For");
        String ipAddress = forwardedFor != null && !forwardedFor.isBlank()
                ? forwardedFor.split(",")[0].trim()
                : request.getRemoteAddr();

        return new ClientRequestMetadata(deviceName, ipAddress, userAgent);
    }
}
