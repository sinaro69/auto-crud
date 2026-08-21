package com.developerscambodia.media.exception;


import com.developerscambodia.media.base.BaseError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ServiceException {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handle(ResponseStatusException e) {

        var baseError = BaseError.builder()
                .code(e.getStatusCode().value())
                .errors(e.getReason())
                .message(e.getMessage())
                .status(false)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(e.getStatusCode()).body(baseError);
    }

}
