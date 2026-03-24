package com.nativeflow.backend.auth.mapper;

import com.nativeflow.backend.auth.model.AuthSessionEntity;
import java.time.OffsetDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthSessionMapper {

    String insertSession(
            @Param("userId") String userId,
            @Param("refreshTokenHash") String refreshTokenHash,
            @Param("deviceName") String deviceName,
            @Param("ipAddress") String ipAddress,
            @Param("userAgent") String userAgent,
            @Param("expiresAt") OffsetDateTime expiresAt
    );

    AuthSessionEntity findActiveSessionById(@Param("sessionId") String sessionId);

    int deactivateSession(@Param("sessionId") String sessionId);
}
