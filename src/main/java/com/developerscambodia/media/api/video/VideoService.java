package com.developerscambodia.media.api.video;


import com.developerscambodia.media.api.video.web.VideoMetaDataDto;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


public interface VideoService {

    Mono<VideoMetaDataDto> uploadVideo(MultipartFile file, String bucketName);

    Flux<VideoMetaDataDto> uploadMultipleVideos(List<MultipartFile> files, String bucketName);

    Flux<List<VideoMetaDataDto>> findAllVideos(VideoMetaDataDto videoMetaDataDto);

    VideoMetaDataDto findVideoByFileName(String fileName);

    VideoMetaDataDto findVideoBuyUuid(String uuid);

    void disableFile(String uuid);

    @Transactional
    void deleteFile(String uuid, Boolean status, String bucketName);

}
