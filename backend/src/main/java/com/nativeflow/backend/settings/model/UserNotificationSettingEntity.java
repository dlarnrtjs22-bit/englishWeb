package com.nativeflow.backend.settings.model;

public class UserNotificationSettingEntity {

    private String userId;
    private boolean dailyReminderEnabled;
    private boolean newContentEnabled;
    private boolean reviewDueEnabled;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isDailyReminderEnabled() {
        return dailyReminderEnabled;
    }

    public void setDailyReminderEnabled(boolean dailyReminderEnabled) {
        this.dailyReminderEnabled = dailyReminderEnabled;
    }

    public boolean isNewContentEnabled() {
        return newContentEnabled;
    }

    public void setNewContentEnabled(boolean newContentEnabled) {
        this.newContentEnabled = newContentEnabled;
    }

    public boolean isReviewDueEnabled() {
        return reviewDueEnabled;
    }

    public void setReviewDueEnabled(boolean reviewDueEnabled) {
        this.reviewDueEnabled = reviewDueEnabled;
    }
}
