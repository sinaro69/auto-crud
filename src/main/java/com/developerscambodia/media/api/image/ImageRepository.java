package com.developerscambodia.media.api.image;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<ImageMetaData, Long> {

    Optional<ImageMetaData> findByUuid(String uuid);

    Optional<ImageMetaData> findByFileName(String fileName);
}
