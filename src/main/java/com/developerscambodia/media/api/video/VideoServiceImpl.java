package com.developerscambodia.media.api.video;

import com.developerscambodia.media.api.video.web.VideoMetaDataDto;
import com.developerscambodia.media.utils.FileUtil;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class VideoServiceImpl implements VideoService {

    private final MinioClient minioClient;
    private final VideoRepository videoRepository;
    private final VideoMapper videoMapper;

    @Value("${stream.uri}")
    private String streamUri;


    @Override
    public Mono<VideoMetaDataDto> uploadVideo(MultipartFile file, String bucketName) {

        String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
        String uniqueIdentifier = UUID.randomUUID().toString();
        String newFileName = String.format("%s.%s", uniqueIdentifier, FileUtil.extractExtension(originalFileName));

        if (!Objects.equals(file.getContentType(), "video/mp4")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only MP4 file is allowed to upload!");
        }

        return Mono.fromCallable(() -> {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(newFileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .build();

            minioClient.putObject(putObjectArgs);

            return VideoMetaDataDto.builder()
                    .uuid(uniqueIdentifier)
                    .fileName(newFileName)
                    .streamUri(streamUri + newFileName)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .duration(null)
                    .isDeleted(false)
                    .createdAt(LocalDateTime.now())
                    .lastModifiedAt(LocalDateTime.now())
                    .build();

        }).flatMap(videoMetaDataDto ->
                Mono.defer(() -> {
                    try {
                        // Simulating the non-reactive save operation
                        VideoMetaData videoMetaData = videoMapper.toVideoMetaData(videoMetaDataDto);
                        videoRepository.save(videoMetaData);
                        return Mono.just(videoMetaDataDto);
                    } catch (Exception e) {
                        // Handle the exception gracefully, maybe log the error
                        System.out.println(e.getMessage());
                        return Mono.empty();
                    }
                })
        );
    }

    @Override
    public Flux<VideoMetaDataDto> uploadMultipleVideos(List<MultipartFile> files, String bucketName) {

        if (files.stream().anyMatch(file -> !Objects.equals(file.getContentType(), "video/mp4"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only MP4 file is allowed to upload!");
        }

        return Flux.fromIterable(files)
                .flatMap(file -> {

                    String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
                    String uniqueIdentifier = UUID.randomUUID().toString();
                    String newFileName = String.format("%s.%s", uniqueIdentifier, FileUtil.extractExtension(originalFileName));

                    return Mono.fromCallable(() -> {

                        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(newFileName)
                                .stream(file.getInputStream(), file.getSize(), -1)
                                .build();

                        minioClient.putObject(putObjectArgs);

                        return VideoMetaDataDto.builder()
                                .uuid(uniqueIdentifier)
                                .fileName(newFileName)
                                .streamUri(streamUri + newFileName)
                                .fileSize(file.getSize())
                                .contentType(file.getContentType())
                                .duration(null)
                                .isDeleted(false)
                                .createdAt(LocalDateTime.now())
                                .lastModifiedAt(LocalDateTime.now())
                                .build();

                    }).flatMap(videoMetaDataDto ->
                            Mono.fromCallable(() -> {
                                // Simulating the non-reactive save operation
                                VideoMetaData videoMetaData = videoMapper.toVideoMetaData(videoMetaDataDto);
                                videoRepository.save(videoMetaData);
                                return videoMetaDataDto;
                            }).onErrorResume(e -> {
                                log.error(e.getMessage());
                                return Mono.empty();
                            })
                    );
                });
    }

    @Override
    public Flux<List<VideoMetaDataDto>> findAllVideos(VideoMetaDataDto videoMetaDataDto) {
        Sort sortById = Sort.by(Sort.Direction.DESC,"id");
        List<VideoMetaData> videoMetaDataList = videoRepository.findAll(sortById);
        return Flux.just(videoMapper.toVideoMetaDataDtoList(videoMetaDataList));
    }

    @Override
    public VideoMetaDataDto findVideoByFileName(String fileName) {
        var video = videoRepository.findByFileName(fileName).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));
        return videoMapper.toVideoDto(video);

    }

    @Override
    public VideoMetaDataDto findVideoBuyUuid(String uuid) {
        var video = videoRepository.findByUuid(uuid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));
        return videoMapper.toVideoDto(video);
    }

    @Override
    public void disableFile(String uuid) {
        VideoMetaData fileMetaData = videoRepository.findByUuid(uuid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));
        fileMetaData.setIsDeleted(true);
        videoRepository.save(fileMetaData);
    }

    @Override
    public void deleteFile(String uuid, Boolean status, String bucketName) {
        String objectName = videoRepository.findByUuid(uuid).orElseThrow().getFileName();

        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            videoRepository.delete(videoRepository.findByUuid(uuid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found")));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }


}
