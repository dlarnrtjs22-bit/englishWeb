package com.nativeflow.backend.common.security;

public record AuthenticatedUser(
        String userId,
        String sessionId,
        String name,
        String email,
        String role
) {
}
