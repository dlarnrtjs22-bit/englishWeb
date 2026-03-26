package com.nativeflow.backend.dto;

import java.util.List;

public final class ApiResponses {

    private ApiResponses() {
    }

    public record SeriesSummaryDto(
            String id,
            String title,
            String subtitle,
            String description,
            String categoryLabel,
            String thumbnailUrl,
            int progress,
            int packCount,
            boolean isSubscribed,
            String badge
    ) {
    }

    public record DashboardResponse(
            String userName,
            int progressPercent,
            String progressMessage,
            ReviewSummaryDto reviewSummary,
            List<SeriesSummaryDto> activeSeries,
            List<SeriesSummaryDto> recommendedSeries,
            List<StatDto> stats
    ) {
    }

    public record ReviewSummaryDto(int dueCount, String description, List<String> priorityLabels) {
    }

    public record StatDto(String label, String value) {
    }

    public record SeriesPackDto(
            String id,
            String unitLabel,
            String title,
            String description,
            int itemCount,
            int progress,
            boolean completed,
            boolean locked,
            String statusLabel,
            String firstItemId
    ) {
    }

    public record SeriesDetailResponse(
            String id,
            String title,
            String description,
            String categoryLabel,
            String thumbnailUrl,
            String instructor,
            String levelLabel,
            String updatedAt,
            int progress,
            String progressMessage,
            String coachNote,
            List<String> tags,
            List<SeriesPackDto> packs
    ) {
    }

    public record LearningProgressDto(int current, int total) {
    }

    public record LearningItemResponse(
            String id,
            String sourceText,
            String targetText,
            String nuanceNote,
            String exampleSentence,
            String exampleTranslation,
            String aiFeedback,
            LearningProgressDto progress
    ) {
    }

    public record FavoriteItemDto(
            String itemId,
            String sourceText,
            String targetText,
            String seriesTitle,
            String packTitle
    ) {
    }

    public record FavoritesResponse(List<FavoriteItemDto> items) {
    }

    public record FavoriteToggleResponse(boolean success, boolean isFavorited) {
    }

    public record CheckAnswerResponse(
            boolean isCorrect,
            String correctAnswer,
            List<String> acceptedAnswers,
            String exampleSentence,
            String exampleTranslation
    ) {
    }

    public record ReviewItemDto(String itemId, String sourceText, String contextText, Integer level) {
    }

    public record ReviewGroupDto(String seriesId, String seriesTitle, String description, List<ReviewItemDto> items) {
    }

    public record ReviewSummaryCardDto(String label, String value, String caption, String icon, String variant) {
    }

    public record ReviewQueueResponse(
            List<ReviewItemDto> items,
            List<ReviewGroupDto> groups,
            List<ReviewSummaryCardDto> summaryCards,
            List<Integer> weeklyHistory
    ) {
    }

    public record ReviewScheduleResponse(
            boolean success,
            String result,
            int intervalDays,
            double easeFactor,
            String nextReviewAt,
            String nextItemId
    ) {
    }

    public record RandomLearningItemResponse(String itemId) {
    }

    public record RandomLearningQueueResponse(List<String> itemIds) {
    }

    public record ActionSuccessResponse(boolean success) {
    }

    public record SettingsProfileDto(String name, String email, String bio) {
    }

    public record LevelOptionDto(String label, String description, boolean active) {
    }

    public record ActionItemDto(String title, String description, String actionLabel) {
    }

    public record NotificationItemDto(String title, String description, boolean enabled) {
    }

    public record SettingsResponse(
            SettingsProfileDto profile,
            String dailyGoal,
            List<LevelOptionDto> learningLevels,
            List<ActionItemDto> accountItems,
            List<NotificationItemDto> notifications
    ) {
    }
}
