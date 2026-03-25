package com.nativeflow.backend.settings.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public final class SettingsDtos {

    private SettingsDtos() {
    }

    public record SettingsResponse(
            ProfileResponse profile,
            String dailyGoal,
            LearningLevelResponse[] learningLevels,
            AccountItemResponse[] accountItems,
            NotificationResponse[] notifications
    ) {
    }

    public record ProfileResponse(
            String name,
            String email,
            String bio
    ) {
    }

    public record LearningLevelResponse(
            String label,
            String description,
            boolean active
    ) {
    }

    public record AccountItemResponse(
            String title,
            String description,
            String actionLabel
    ) {
    }

    public record NotificationResponse(
            String title,
            String description,
            boolean enabled
    ) {
    }

    public record UpdateSettingsRequest(
            @Valid ProfileUpdateRequest profile,
            @Valid PreferenceUpdateRequest preference,
            @Valid NotificationUpdateRequest notifications
    ) {
    }

    public record ProfileUpdateRequest(
            @NotBlank(message = "이름은 필수입니다.") String name,
            String bio
    ) {
    }

    public record PreferenceUpdateRequest(
            @NotBlank(message = "레벨은 필수입니다.") String level,
            @Min(value = 1, message = "일일 목표는 1 이상이어야 합니다.") @Max(value = 500, message = "일일 목표는 500 이하여야 합니다.") int dailyGoal,
            @NotBlank(message = "인터페이스 언어는 필수입니다.") String interfaceLanguage,
            @NotBlank(message = "목표 언어는 필수입니다.") String targetLanguage
    ) {
    }

    public record NotificationUpdateRequest(
            boolean dailyReminderEnabled,
            boolean newContentEnabled,
            boolean reviewDueEnabled
    ) {
    }
}
