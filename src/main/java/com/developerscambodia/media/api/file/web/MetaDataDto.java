package com.developerscambodia.media.api.file.web;


import java.time.LocalDateTime;


public record MetaDataDto(
        String uuid,
        String fileName,
        String contentType,
        Long fileSize,
        String fileUrl,
        Boolean isDeleted,
        LocalDateTime createdAt,
        LocalDateTime lastModifiedAt



) {

}
