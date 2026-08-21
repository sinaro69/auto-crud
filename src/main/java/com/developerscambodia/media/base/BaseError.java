package com.developerscambodia.media.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BaseError<T>(Integer code,
                           @JsonInclude(JsonInclude.Include.NON_NULL)
                           T errors,
                           String message,
                           Boolean status,
                           @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
                           LocalDateTime timestamp) {

}

