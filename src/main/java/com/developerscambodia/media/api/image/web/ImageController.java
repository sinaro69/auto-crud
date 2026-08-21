package com.developerscambodia.media.api.image.web;

import com.developerscambodia.media.api.image.ImageMetaData;
import com.developerscambodia.media.api.image.ImageService;
import com.developerscambodia.media.base.BaseApi;
import com.developerscambodia.media.base.BaseResponse;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/images")
public class ImageController {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    private final ImageService imageService;

    @PostMapping(value = {"/", ""})
    public Mono<BaseApi<?>> uploadImage(@RequestParam("file") MultipartFile file){
        ImageMetaData metaData = imageService.uploadImage(file, "images").block();
        return Mono.just(BaseApi.builder()
                .code(HttpStatus.CREATED.value())
                .message("Image has been uploaded successfully.")
                .timestamp(LocalDateTime.now())
                .data(metaData)
                .status(true)
                .build());

    }

    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Flux<BaseApi<?>> uploadMultipleImages(@RequestPart("files") List<MultipartFile> files) {

        Flux<ImageMetaData> uploadedImages = imageService.uploadMultipleImages(files, "images");
        return uploadedImages
                .map(imageMetaData -> BaseApi.builder()
                        .code(HttpStatus.CREATED.value())
                        .message("Image has been uploaded successfully.")
                        .timestamp(LocalDateTime.now())
                        .data(imageMetaData)
                        .status(true)
                        .build()
                );
    }

    @GetMapping(value = {"/", ""})
    public Flux<BaseApi<?>> findAllImage(ImageMetaDataDto imageMetaDataDto) {
        return imageService.findAllImage(imageMetaDataDto)
                .map(imageMetaDataList -> BaseApi.builder()
                        .code(HttpStatus.OK.value())
                        .message("Images have been found successfully.")
                        .timestamp(LocalDateTime.now())
                        .data(imageMetaDataList)
                        .status(true)
                        .build()
                );
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/view/{fileName}")
    public ResponseEntity<InputStreamResource> viewByFileName(@PathVariable String fileName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        ImageMetaDataDto image = imageService.viewByFileName(fileName);
        System.out.println(image.contentType());
        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .contentLength(image.fileSize())
                //.header("Content-disposition", "attachment; filename=" + image.fileName()) // It will automatically download
                .body(image.stream());
    }

    @GetMapping("/{uuid}")
    public BaseApi<?> findImageByUuid(@PathVariable String uuid) {
        ImageMetaDataDto image = imageService.findImageByUuid(uuid);
        return BaseApi.builder()
                .code(HttpStatus.OK.value())
                .message("Image has been found successfully.")
                .timestamp(LocalDateTime.now())
                .data(image)
                .status(true)
                .build();
    }

    @PutMapping("/{uuid}/disable")
    public BaseApi<?> disableImage(@PathVariable String uuid) {
        imageService.disableImage(uuid);
        return BaseApi.builder()
                .code(HttpStatus.OK.value())
                .message("Image has been disabled successfully.")
                .timestamp(LocalDateTime.now())
                .status(true)
                .build();
    }

    @DeleteMapping("/{uuid}")
    public BaseApi<Object> deleteImage(@PathVariable String uuid) {
        imageService.deleteImage(uuid, "images");
        return BaseApi.builder()
                .code(HttpStatus.OK.value())
                .message("Image has been deleted successfully.")
                .timestamp(LocalDateTime.now())
                .status(true)
                .build();
    }

}

