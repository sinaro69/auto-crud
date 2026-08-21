package com.developerscambodia.media.api.video;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoRepository extends JpaRepository<VideoMetaData, Long> {

     Optional<VideoMetaData> findByUuid(String uuid);

     Optional<VideoMetaData> findByFileName(String fileName);



}
