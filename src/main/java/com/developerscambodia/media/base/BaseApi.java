package com.developerscambodia.media.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BaseApi<T>(Integer code,
                         @JsonInclude(JsonInclude.Include.NON_NULL)
                         T data,
                         String message,
                         Boolean status,
                         @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
                         LocalDateTime timestamp) {
}
