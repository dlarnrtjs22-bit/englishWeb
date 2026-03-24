package com.nativeflow.backend.common.security;

public record AuthenticatedUser(
        String userId,
        String sessionId,
        String email,
        String role
) {
}
