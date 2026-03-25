package com.nativeflow.backend.learning.dto;

import jakarta.validation.constraints.NotBlank;

public final class LearningSessionDtos {

    private LearningSessionDtos() {
    }

    public record StartStudySessionRequest(String seriesId) {
    }

    public record StartReviewSessionRequest(String source, String itemId) {
    }

    public record SessionCardResponse(
            String id,
            String sourceText,
            String targetText,
            String nuanceNote,
            String exampleSentence,
            String exampleTranslation
    ) {
    }

    public record SessionProgressResponse(int current, int total) {
    }

    public record SessionSummaryResponse(String title, String message, int completedCount, int totalCount) {
    }

    public record LearningSessionResponse(
            String sessionId,
            String sessionType,
            String sessionSource,
            boolean completed,
            SessionProgressResponse progress,
            SessionCardResponse currentCard,
            SessionSummaryResponse summary
    ) {
    }

    public record SessionAnswerRequest(@NotBlank(message = "정답 입력값은 필수입니다.") String typedAnswer) {
    }

    public record SessionRateRequest(@NotBlank(message = "평가 결과는 필수입니다.") String result) {
    }
}
