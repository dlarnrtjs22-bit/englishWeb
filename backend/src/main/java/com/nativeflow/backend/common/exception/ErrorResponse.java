package com.nativeflow.backend.common.exception;

import java.time.OffsetDateTime;

public record ErrorResponse(
        String code,
        String message,
        int status,
        OffsetDateTime timestamp
) {
}
