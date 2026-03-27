package com.nativeflow.backend.diary.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public final class DiaryDtos {

    private DiaryDtos() {
    }

    public record DiaryFeedbackLineDto(
            String originalLine,
            String correctedLine,
            String translationLine
    ) {
    }

    public record DiaryFeedbackNoteDto(
            String type,
            String content
    ) {
    }

    public record DiaryFeedbackResponse(
            boolean perfect,
            String headline,
            String summary,
            String correctedContent,
            String modelName,
            String correctedAt,
            List<DiaryFeedbackLineDto> lines,
            List<String> keywords,
            List<String> tips,
            List<String> advice
    ) {
    }

    public record DiaryEntryResponse(
            String entryDate,
            boolean exists,
            String rawContent,
            DiaryFeedbackResponse feedback
    ) {
    }

    public record DiaryCalendarResponse(List<String> writtenDates) {
    }

    public record DiaryHistoryItemResponse(
            String entryDate,
            String rawSnippet,
            String correctedSnippet,
            List<String> keywords
    ) {
    }

    public record DiaryHistoryResponse(List<DiaryHistoryItemResponse> items) {
    }

    public record DiaryFeedbackRequest(
            @NotBlank(message = "날짜는 필수입니다.") String entryDate,
            @NotBlank(message = "일기 내용은 필수입니다.") String rawContent
    ) {
    }

    public record SaveDiaryEntryRequest(
            @NotBlank(message = "일기 내용은 필수입니다.") String rawContent,
            @Valid DiaryFeedbackPayload feedback
    ) {
    }

    public record DiaryFeedbackPayload(
            boolean perfect,
            String headline,
            String summary,
            String correctedContent,
            String modelName,
            String correctedAt,
            List<@Valid DiaryFeedbackLinePayload> lines,
            List<@NotBlank(message = "주요 단어 내용은 비워둘 수 없습니다.") String> keywords,
            List<@NotBlank(message = "팁 내용은 비워둘 수 없습니다.") String> tips,
            List<@NotBlank(message = "조언 내용은 비워둘 수 없습니다.") String> advice
    ) {
    }

    public record DiaryFeedbackLinePayload(
            String originalLine,
            @NotBlank(message = "첨삭 문장은 필수입니다.") String correctedLine,
            String translationLine
    ) {
    }
}
