package com.developerscambodia.media.api.image;

import com.developerscambodia.media.api.image.web.ImageMetaDataDto;
import io.minio.errors.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface ImageService {

    Mono<ImageMetaData> uploadImage(MultipartFile file, String bucketName);
    Flux<ImageMetaData> uploadMultipleImages(List<MultipartFile> files, String bucketName);
    Flux<List<ImageMetaDataDto>> findAllImage(ImageMetaDataDto imageMetaDataDto);
    ImageMetaDataDto viewByFileName(String fileName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;
    ImageMetaDataDto findImageByUuid(String uuid);
    void disableImage(String uuid);
    @Transactional
    void deleteImage(String uuid, String bucketName);

    String getPresignedUrl(String bucketName, String fileName);


}
