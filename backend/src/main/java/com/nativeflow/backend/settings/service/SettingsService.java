package com.nativeflow.backend.settings.service;

import com.nativeflow.backend.auth.model.UserProfileEntity;
import com.nativeflow.backend.settings.dto.SettingsDtos;
import com.nativeflow.backend.settings.mapper.SettingsMapper;
import com.nativeflow.backend.settings.model.UserNotificationSettingEntity;
import com.nativeflow.backend.settings.model.UserPreferenceEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingsService {

    private final SettingsMapper settingsMapper;

    public SettingsService(SettingsMapper settingsMapper) {
        this.settingsMapper = settingsMapper;
    }

    @Transactional
    public void createDefaultSettings(String userId) {
        settingsMapper.insertDefaultPreferences(userId);
        settingsMapper.insertDefaultNotificationSettings(userId);
    }

    public SettingsDtos.SettingsResponse getSettings(String userId) {
        UserProfileEntity profile = settingsMapper.findProfileByUserId(userId);
        UserPreferenceEntity preference = settingsMapper.findPreferenceByUserId(userId);
        UserNotificationSettingEntity notification = settingsMapper.findNotificationSettingByUserId(userId);

        return new SettingsDtos.SettingsResponse(
                new SettingsDtos.ProfileResponse(
                        profile.getName(),
                        profile.getEmail(),
                        preference != null ? safeText(preference.getBio()) : ""
                ),
                (preference != null ? preference.getDailyGoal() : 20) + " 표현",
                buildLearningLevels(preference != null ? preference.getLevel() : "intermediate"),
                new SettingsDtos.AccountItemResponse[] {
                        new SettingsDtos.AccountItemResponse("비밀번호 변경", "마지막 변경 시점은 추후 보안 로그와 연결됩니다.", "변경"),
                        new SettingsDtos.AccountItemResponse("인터페이스 언어", (preference != null ? preference.getInterfaceLanguage() : "ko") + " / English", "전환"),
                        new SettingsDtos.AccountItemResponse("계정 비활성화", "구독 및 학습 데이터와 함께 처리됩니다.", "요청")
                },
                new SettingsDtos.NotificationResponse[] {
                        new SettingsDtos.NotificationResponse("일일 학습 리마인더", "설정한 시간에 학습 알림을 보냅니다.", notification == null || notification.isDailyReminderEnabled()),
                        new SettingsDtos.NotificationResponse("신규 콘텐츠 알림", "새로운 시리즈와 업데이트 소식을 알려줍니다.", notification == null || notification.isNewContentEnabled()),
                        new SettingsDtos.NotificationResponse("복습 큐 알림", "복습 due 시점이 지나면 알려줍니다.", notification == null || notification.isReviewDueEnabled())
                }
        );
    }

    @Transactional
    public SettingsDtos.SettingsResponse updateSettings(String userId, SettingsDtos.UpdateSettingsRequest request) {
        settingsMapper.updateUserProfile(
                userId,
                request.profile().name().trim(),
                request.profile().bio(),
                request.preference().targetLanguage().trim().toLowerCase()
        );
        settingsMapper.updateUserPreference(
                userId,
                request.preference().level().trim().toLowerCase(),
                request.preference().dailyGoal(),
                request.preference().interfaceLanguage().trim().toLowerCase(),
                request.profile().bio()
        );
        settingsMapper.updateNotificationSetting(
                userId,
                request.notifications().dailyReminderEnabled(),
                request.notifications().newContentEnabled(),
                request.notifications().reviewDueEnabled()
        );

        return getSettings(userId);
    }

    private SettingsDtos.LearningLevelResponse[] buildLearningLevels(String activeLevel) {
        return new SettingsDtos.LearningLevelResponse[] {
                new SettingsDtos.LearningLevelResponse("Beginner", "초급", "beginner".equals(activeLevel)),
                new SettingsDtos.LearningLevelResponse("Intermediate", "중급", "intermediate".equals(activeLevel)),
                new SettingsDtos.LearningLevelResponse("Advanced", "고급", "advanced".equals(activeLevel))
        };
    }

    private String safeText(String text) {
        return text == null ? "" : text;
    }
}
