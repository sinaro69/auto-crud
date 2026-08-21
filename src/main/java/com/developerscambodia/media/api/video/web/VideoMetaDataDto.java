package com.developerscambodia.media.api.video.web;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record VideoMetaDataDto(
        String uuid,
        String fileName,
        String streamUri,
        Long fileSize,
        String contentType,
        String duration,
        Boolean isDeleted,
        LocalDateTime createdAt,
        LocalDateTime lastModifiedAt
) {


}
