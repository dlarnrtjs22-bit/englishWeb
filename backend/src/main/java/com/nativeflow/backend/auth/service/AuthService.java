package com.nativeflow.backend.auth.service;

import com.nativeflow.backend.auth.dto.AuthDtos;
import com.nativeflow.backend.auth.mapper.AuthMapper;
import com.nativeflow.backend.auth.mapper.AuthSessionMapper;
import com.nativeflow.backend.auth.mapper.SubscriptionMapper;
import com.nativeflow.backend.auth.model.AuthSessionEntity;
import com.nativeflow.backend.auth.model.AuthUserEntity;
import com.nativeflow.backend.auth.model.SubscriptionAccessEntity;
import com.nativeflow.backend.auth.model.UserProfileEntity;
import com.nativeflow.backend.common.exception.ApiException;
import com.nativeflow.backend.common.exception.ErrorCode;
import com.nativeflow.backend.common.security.ClientRequestMetadata;
import com.nativeflow.backend.common.security.JwtTokenProvider;
import com.nativeflow.backend.content.service.ContentService;
import com.nativeflow.backend.settings.service.SettingsService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Pattern PASSWORD_POLICY = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$"
    );
    private static final String DEFAULT_PLAN_CODE = "standard-monthly";

    private final AuthMapper authMapper;
    private final AuthSessionMapper authSessionMapper;
    private final SubscriptionMapper subscriptionMapper;
    private final ContentService contentService;
    private final SettingsService settingsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            AuthMapper authMapper,
            AuthSessionMapper authSessionMapper,
            SubscriptionMapper subscriptionMapper,
            ContentService contentService,
            SettingsService settingsService,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.authMapper = authMapper;
        this.authSessionMapper = authSessionMapper;
        this.subscriptionMapper = subscriptionMapper;
        this.contentService = contentService;
        this.settingsService = settingsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthDtos.AuthResponse signup(AuthDtos.SignupRequest request, ClientRequestMetadata metadata) {
        String normalizedEmail = normalizeEmail(request.email());
        String normalizedName = request.name().trim();
        String normalizedTargetLanguage = request.targetLanguage().trim().toLowerCase();

        validatePasswordPolicy(request.password());

        if (authMapper.countByEmail(normalizedEmail) > 0) {
            throw new ApiException(ErrorCode.DUPLICATE_EMAIL, HttpStatus.CONFLICT, "이미 가입된 이메일입니다.");
        }

        String userId = authMapper.insertUser(
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                normalizedName,
                "email",
                "user",
                "ko",
                normalizedTargetLanguage
        );

        OffsetDateTime periodStart = OffsetDateTime.now();
        OffsetDateTime periodEnd = periodStart.plusMonths(1);
        subscriptionMapper.insertUserSubscription(userId, DEFAULT_PLAN_CODE, "trialing", periodStart, periodEnd);
        settingsService.createDefaultSettings(userId);
        contentService.syncPublishedSeriesAccess(userId);

        UserProfileEntity profile = requireUserProfile(userId);
        SubscriptionAccessEntity subscription = requireAccessibleSubscription(userId);
        return issueTokens(profile, subscription, metadata);
    }

    @Transactional
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request, ClientRequestMetadata metadata) {
        AuthUserEntity authUser = authMapper.findAuthUserByEmail(normalizeEmail(request.email()));

        if (authUser == null || authUser.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), authUser.getPasswordHash())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        UserProfileEntity profile = requireUserProfile(authUser.getId());
        SubscriptionAccessEntity subscription = requireAccessibleSubscription(authUser.getId());
        contentService.syncPublishedSeriesAccess(authUser.getId());
        return issueTokens(profile, subscription, metadata);
    }

    public AuthDtos.UserProfileResponse me(String userId) {
        UserProfileEntity profile = requireUserProfile(userId);
        SubscriptionAccessEntity subscription = requireAccessibleSubscription(userId);
        contentService.syncPublishedSeriesAccess(userId);
        return toProfileResponse(profile, subscription);
    }

    @Transactional
    public AuthDtos.LogoutResponse logout(String sessionId) {
        authSessionMapper.deactivateSession(sessionId);
        return new AuthDtos.LogoutResponse(true);
    }

    public AuthSessionEntity requireActiveSession(String sessionId, String userId) {
        AuthSessionEntity session = authSessionMapper.findActiveSessionById(sessionId);

        if (session == null || !session.isActive() || !session.getUserId().equals(userId)
                || session.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new ApiException(ErrorCode.SESSION_NOT_FOUND, HttpStatus.UNAUTHORIZED, "유효한 로그인 세션이 아닙니다.");
        }

        return session;
    }

    private AuthDtos.AuthResponse issueTokens(
            UserProfileEntity profile,
            SubscriptionAccessEntity subscription,
            ClientRequestMetadata metadata
    ) {
        String refreshToken = generateRefreshToken();
        String sessionId = authSessionMapper.insertSession(
                profile.getId(),
                hashToken(refreshToken),
                metadata.deviceName(),
                metadata.ipAddress(),
                metadata.userAgent(),
                OffsetDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenValiditySeconds())
        );

        String accessToken = jwtTokenProvider.createAccessToken(
                profile.getId(),
                sessionId,
                profile.getName(),
                profile.getEmail(),
                profile.getRole()
        );

        return new AuthDtos.AuthResponse(
                toProfileResponse(profile, subscription),
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenValiditySeconds()
        );
    }

    private UserProfileEntity requireUserProfile(String userId) {
        UserProfileEntity profile = authMapper.findUserProfileById(userId);

        if (profile == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND, "사용자 정보를 찾을 수 없습니다.");
        }

        return profile;
    }

    private SubscriptionAccessEntity requireAccessibleSubscription(String userId) {
        SubscriptionAccessEntity subscription = subscriptionMapper.findAccessibleSubscription(userId);

        if (subscription == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "활성 구독이 없어 로그인할 수 없습니다.");
        }

        return subscription;
    }

    private AuthDtos.UserProfileResponse toProfileResponse(
            UserProfileEntity profile,
            SubscriptionAccessEntity subscription
    ) {
        return new AuthDtos.UserProfileResponse(
                profile.getId(),
                profile.getName(),
                profile.getEmail(),
                profile.getRole(),
                subscription.getPlanName(),
                profile.getNativeLanguage(),
                profile.getTargetLanguage(),
                subscription.getStatus(),
                subscription.getCurrentPeriodEnd().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        );
    }

    private void validatePasswordPolicy(String password) {
        if (!PASSWORD_POLICY.matcher(password).matches()) {
            throw new ApiException(
                    ErrorCode.PASSWORD_POLICY_VIOLATION,
                    HttpStatus.BAD_REQUEST,
                    "비밀번호는 8자 이상이며 영문 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다."
            );
        }
    }

    private String generateRefreshToken() {
        return UUID.randomUUID() + "." + UUID.randomUUID();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available.", exception);
        }
    }
}
