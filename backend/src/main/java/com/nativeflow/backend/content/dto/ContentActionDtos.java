package com.nativeflow.backend.content.dto;

import jakarta.validation.constraints.NotBlank;

public final class ContentActionDtos {

    private ContentActionDtos() {
    }

    public record CheckAnswerRequest(
            @NotBlank(message = "정답 입력값은 필수입니다.") String typedAnswer
    ) {
    }

    public record ReviewRequest(
            @NotBlank(message = "복습 결과는 필수입니다.") String result
    ) {
    }
}
