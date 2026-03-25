package com.nativeflow.backend.common.security;

import com.nativeflow.backend.auth.model.AuthSessionEntity;
import com.nativeflow.backend.auth.service.AuthService;
import com.nativeflow.backend.common.exception.ApiException;
import com.nativeflow.backend.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthInterceptor(AuthService authService, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        String token = authorization.substring(7);
        Claims claims = jwtTokenProvider.parse(token);
        String userId = claims.getSubject();
        String sessionId = claims.get("sid", String.class);
        String name = claims.get("name", String.class);
        String email = claims.get("email", String.class);
        String role = claims.get("role", String.class);

        AuthSessionEntity session = authService.requireActiveSession(sessionId, userId);
        request.setAttribute(
                CurrentUserArgumentResolver.CURRENT_USER_ATTRIBUTE,
                new AuthenticatedUser(session.getUserId(), session.getId(), name, email, role)
        );
        return true;
    }
}
