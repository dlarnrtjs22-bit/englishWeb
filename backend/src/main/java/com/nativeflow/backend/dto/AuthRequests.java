package com.nativeflow.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class AuthRequests {

    private AuthRequests() {
    }

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}

    public record SignupRequest(
            @NotBlank String name,
            @Email @NotBlank String email,
            @NotBlank String password,
            @NotBlank String targetLanguage
    ) {}
}
