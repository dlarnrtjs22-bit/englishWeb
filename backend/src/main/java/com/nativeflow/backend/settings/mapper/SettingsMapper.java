package com.nativeflow.backend.settings.mapper;

import com.nativeflow.backend.auth.model.UserProfileEntity;
import com.nativeflow.backend.settings.model.UserNotificationSettingEntity;
import com.nativeflow.backend.settings.model.UserPreferenceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SettingsMapper {

    void insertDefaultPreferences(@Param("userId") String userId);

    void insertDefaultNotificationSettings(@Param("userId") String userId);

    UserPreferenceEntity findPreferenceByUserId(@Param("userId") String userId);

    UserNotificationSettingEntity findNotificationSettingByUserId(@Param("userId") String userId);

    void updateUserProfile(
            @Param("userId") String userId,
            @Param("name") String name,
            @Param("bio") String bio,
            @Param("targetLanguage") String targetLanguage
    );

    void updateUserPreference(
            @Param("userId") String userId,
            @Param("level") String level,
            @Param("dailyGoal") int dailyGoal,
            @Param("interfaceLanguage") String interfaceLanguage,
            @Param("bio") String bio
    );

    void updateNotificationSetting(
            @Param("userId") String userId,
            @Param("dailyReminderEnabled") boolean dailyReminderEnabled,
            @Param("newContentEnabled") boolean newContentEnabled,
            @Param("reviewDueEnabled") boolean reviewDueEnabled
    );

    UserProfileEntity findProfileByUserId(@Param("userId") String userId);
}
