package com.nativeflow.backend.content.dto;

import jakarta.validation.constraints.NotBlank;

public final class ContentActionDtos {

    private ContentActionDtos() {
    }

    public record CheckAnswerRequest(String typedAnswer, String mode) {
    }

    public record SentenceFeedbackRequest(
            @NotBlank(message = "문장은 필수입니다.") String sentence,
            String mode
    ) {
    }

    public record ReviewRequest(
            @NotBlank(message = "복습 결과는 필수입니다.") String result,
            String mode
    ) {
    }
}
