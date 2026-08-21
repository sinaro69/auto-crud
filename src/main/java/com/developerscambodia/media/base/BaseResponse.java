package com.developerscambodia.media.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;
@Builder
public record BaseResponse<T>(
        Integer code,
        String message,
        LocalDateTime timestamp,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        T data
) {
}
