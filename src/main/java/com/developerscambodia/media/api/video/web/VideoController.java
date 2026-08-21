package com.developerscambodia.media.api.video.web;


import com.developerscambodia.media.base.BaseApi;
import com.developerscambodia.media.base.BaseResponse;
import com.developerscambodia.media.api.video.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping(value = "/videos", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @PostMapping(value = {"/", ""}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BaseApi<?>> uploadVideo(@RequestParam MultipartFile file) {

        return videoService.uploadVideo(file, "video-upload")
                .map(videoMetaData -> BaseApi.builder()
                        .code(HttpStatus.CREATED.value())
                        .message("Video has been uploaded successfully")
                        .timestamp(LocalDateTime.now())
                        .data(videoMetaData)
                        .status(true)
                        .build()
                );
    }

    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<BaseApi<?>> uploadMultipleVideos(@RequestParam List<MultipartFile> files) {
        Flux<VideoMetaDataDto> uploadedFiles = videoService.uploadMultipleVideos(files, "video-upload");
        return uploadedFiles.collectList()
                .map(videoMetaDataList -> BaseApi.builder()
                        .code(HttpStatus.CREATED.value())
                        .message("Videos have been uploaded successfully")
                        .timestamp(LocalDateTime.now())
                        .data(videoMetaDataList)
                        .status(true)
                        .build()
                );


    }

    @GetMapping(value = {"/", ""})
    public Flux<BaseResponse<?>> findAllVideos(VideoMetaDataDto videoMetaDataDto) {
        return videoService.findAllVideos(videoMetaDataDto)
                .map(videoMetaDataList -> BaseResponse.builder()
                        .code(HttpStatus.OK.value())
                        .message("Videos have been found successfully.")
                        .timestamp(LocalDateTime.now())
                        .data(videoMetaDataList)
                        .build()
                );
    }

    @GetMapping("/name/{name}")
    public BaseApi<?> findVideoByName(@PathVariable String name) {

        VideoMetaDataDto video = videoService.findVideoByFileName(name);

        return BaseApi.builder()
                .code(HttpStatus.OK.value())
                .message("Video has been found successfully.")
                .timestamp(LocalDateTime.now())
                .data(video)
                .status(true)
                .build();
    }

    @GetMapping("/{uuid}")
    public BaseApi<?> findVideoByUuid(@PathVariable String uuid) {
        VideoMetaDataDto video = videoService.findVideoBuyUuid(uuid);
        return BaseApi.builder()
                .code(HttpStatus.OK.value())
                .message("Video has been found successfully.")
                .timestamp(LocalDateTime.now())
                .data(video)
                .status(true)
                .build();
    }

    @PutMapping("/{uuid}/disable")
    public BaseApi<?> disableVideo(@PathVariable String uuid) {
        videoService.disableFile(uuid);
        return BaseApi.builder()
                .code(HttpStatus.OK.value())
                .message("Video has been disabled successfully.")
                .timestamp(LocalDateTime.now())
                .status(true)
                .build();
    }

    @DeleteMapping("/{uuid}")
    public BaseApi<?> deleteVideo(@PathVariable String uuid, Boolean status) {
        videoService.deleteFile(uuid, status, "video-upload");
        return BaseApi.builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Video has been deleted successfully.")
                .timestamp(LocalDateTime.now())
                .status(true)
                .build();
    }
}
