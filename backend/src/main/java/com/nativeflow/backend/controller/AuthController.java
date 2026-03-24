package com.nativeflow.backend.controller;

import com.nativeflow.backend.dto.ApiResponses;
import com.nativeflow.backend.dto.AuthRequests;
import com.nativeflow.backend.service.MockApiService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final MockApiService mockApiService;

    public AuthController(MockApiService mockApiService) {
        this.mockApiService = mockApiService;
    }

    @PostMapping("/login")
    public ApiResponses.AuthResponse login(@Valid @RequestBody AuthRequests.LoginRequest request) {
        return mockApiService.login(request);
    }

    @PostMapping("/signup")
    public ApiResponses.AuthResponse signup(@Valid @RequestBody AuthRequests.SignupRequest request) {
        return mockApiService.signup(request);
    }
}
