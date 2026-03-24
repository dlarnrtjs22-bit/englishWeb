package com.nativeflow.backend.controller;

import com.nativeflow.backend.auth.dto.AuthDtos;
import com.nativeflow.backend.auth.service.AuthService;
import com.nativeflow.backend.common.security.AuthenticatedUser;
import com.nativeflow.backend.common.security.ClientRequestMetadataResolver;
import com.nativeflow.backend.common.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final ClientRequestMetadataResolver clientRequestMetadataResolver;

    public AuthController(
            AuthService authService,
            ClientRequestMetadataResolver clientRequestMetadataResolver
    ) {
        this.authService = authService;
        this.clientRequestMetadataResolver = clientRequestMetadataResolver;
    }

    @PostMapping("/login")
    public AuthDtos.AuthResponse login(
            @Valid @RequestBody AuthDtos.LoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return authService.login(request, clientRequestMetadataResolver.resolve(httpServletRequest));
    }

    @PostMapping("/signup")
    public AuthDtos.AuthResponse signup(
            @Valid @RequestBody AuthDtos.SignupRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return authService.signup(request, clientRequestMetadataResolver.resolve(httpServletRequest));
    }

    @PostMapping("/logout")
    public AuthDtos.LogoutResponse logout(@CurrentUser AuthenticatedUser authenticatedUser) {
        return authService.logout(authenticatedUser.sessionId());
    }

    @GetMapping("/me")
    public AuthDtos.UserProfileResponse me(@CurrentUser AuthenticatedUser authenticatedUser) {
        return authService.me(authenticatedUser.userId());
    }
}
