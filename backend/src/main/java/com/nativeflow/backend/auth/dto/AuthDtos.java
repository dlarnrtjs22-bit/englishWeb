package com.nativeflow.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record SignupRequest(
            @NotBlank(message = "이름은 필수입니다.") String name,
            @Email(message = "올바른 이메일 형식이 아닙니다.") @NotBlank(message = "이메일은 필수입니다.") String email,
            @NotBlank(message = "비밀번호는 필수입니다.") String password,
            @NotBlank(message = "목표 언어는 필수입니다.") String targetLanguage
    ) {
    }

    public record LoginRequest(
            @Email(message = "올바른 이메일 형식이 아닙니다.") @NotBlank(message = "이메일은 필수입니다.") String email,
            @NotBlank(message = "비밀번호는 필수입니다.") String password
    ) {
    }

    public record UserProfileResponse(
            String id,
            String name,
            String email,
            String role,
            String membershipLabel,
            String nativeLanguage,
            String targetLanguage
    ) {
    }

    public record AuthResponse(
            UserProfileResponse user,
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn
    ) {
    }

    public record LogoutResponse(boolean success) {
    }
}
