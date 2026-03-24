package com.nativeflow.backend.auth.mapper;

import com.nativeflow.backend.auth.model.AuthUserEntity;
import com.nativeflow.backend.auth.model.UserProfileEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthMapper {

    int countByEmail(@Param("email") String email);

    String insertUser(
            @Param("email") String email,
            @Param("passwordHash") String passwordHash,
            @Param("name") String name,
            @Param("authProvider") String authProvider,
            @Param("role") String role,
            @Param("nativeLanguage") String nativeLanguage,
            @Param("targetLanguage") String targetLanguage
    );

    AuthUserEntity findAuthUserByEmail(@Param("email") String email);

    UserProfileEntity findUserProfileById(@Param("userId") String userId);
}
