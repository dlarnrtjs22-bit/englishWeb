package com.nativeflow.backend.chatgpt.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public final class ChatGptDtos {

    private ChatGptDtos() {
    }

    public record ChatMessageRequest(
            @NotBlank(message = "메시지는 필수입니다.") String message,
            String model,
            String conversationId,
            String parentMessageId
    ) {
    }

    public record ConversationTurnRequest(
            @NotBlank(message = "role은 필수입니다.") String role,
            @NotBlank(message = "content는 필수입니다.") String content
    ) {
    }

    public record CodexMessageRequest(
            @NotEmpty(message = "대화 이력은 최소 1개 이상이어야 합니다.")
            List<@Valid ConversationTurnRequest> conversationHistory,
            String model,
            String instructions,
            String reasoningEffort
    ) {
    }

    public record SupportedEffortDto(String effort, String description) {
    }

    public record CodexModelDto(
            String slug,
            String title,
            String description,
            String defaultEffort,
            List<SupportedEffortDto> supportedEfforts
    ) {
    }

    public record ChatGptStatusResponse(
            boolean ready,
            String authMode,
            String accountId,
            String tokenFile,
            String lastRefresh
    ) {
    }

    public record SentenceFeedbackResult(
            boolean perfect,
            String headline,
            String message,
            String correctedSentence,
            List<String> tips
    ) {
    }

    public record DiaryFeedbackLineResult(
            String originalLine,
            String correctedLine,
            String translationLine
    ) {
    }

    public record DiaryFeedbackResult(
            boolean perfect,
            String headline,
            String summary,
            String correctedContent,
            String modelName,
            List<DiaryFeedbackLineResult> lines,
            List<String> keywords,
            List<String> tips,
            List<String> advice
    ) {
    }
}
