package com.nativeflow.backend.content.dto;

public final class ContentActionDtos {

    private ContentActionDtos() {
    }

    public record CheckAnswerRequest(String typedAnswer, String mode) {
    }

    public record ReviewRequest(
            @jakarta.validation.constraints.NotBlank(message = "복습 결과는 필수입니다.") String result,
            String mode
    ) {
    }
}
