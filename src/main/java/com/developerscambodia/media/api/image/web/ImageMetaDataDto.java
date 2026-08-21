package com.developerscambodia.media.api.image.web;

import lombok.Builder;
import org.springframework.core.io.InputStreamResource;

import java.time.LocalDateTime;

@Builder
public record ImageMetaDataDto(String fileName,
                               String contentType,
                               Long fileSize,
                               InputStreamResource stream) {
}
