package com.developerscambodia.media.api.image;

import com.developerscambodia.media.api.image.web.ImageMetaDataDto;
import io.minio.*;

import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;


import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
@RequiredArgsConstructor
@Service
public class ImageServiceImpl implements ImageService{

    private final MinioClient minioClient;
    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;




    @Override
    public Mono<ImageMetaData> uploadImage(MultipartFile file, String bucketName) {

        String uniqueIdentifier = UUID.randomUUID().toString();
        String objectName = uniqueIdentifier + "_" + file.getOriginalFilename();

        if (!Objects.equals(file.getContentType(), "image/jpeg") &&
                !Objects.equals(file.getContentType(), "image/png")) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPG and PNG files are allowed to upload!"));
        }

        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .headers(Map.of("Content-Disposition", "inline"))
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .build();

            minioClient.putObject(putObjectArgs);

            String url = getPresignedUrl(bucketName, objectName);
            String imageUrl = url.contains("?") ? url.substring(0, url.indexOf("?")) : url;

            ImageMetaData imageMetaData = ImageMetaData.builder()
                    .uuid(UUID.randomUUID().toString())
                    .fileName(objectName)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .imageUrl(imageUrl)
                    .build();

            ImageMetaData savedMetaData = imageRepository.save(imageMetaData);
            return Mono.just(savedMetaData);
        } catch (Exception e) {
            return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload the image."));
        }
    }

    @Override
    public Flux<ImageMetaData> uploadMultipleImages(List<MultipartFile> files, String bucketName) {
        if (!files.stream().allMatch(file -> Objects.equals(file.getContentType(), "image/jpeg") &&
                Objects.equals(file.getContentType(), "image/png"))
                ) {
            return Flux.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPG and PNG files are allowed to upload!"));
        }
        return Flux.fromIterable(files)
                .flatMap(file -> {
                    String uniqueIdentifier = UUID.randomUUID().toString();
                    String objectName = uniqueIdentifier + "_" + file.getOriginalFilename();

                    return Mono.fromCallable(() -> {
                        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(file.getInputStream(), file.getSize(), -1)
                                .build();
                        minioClient.putObject(putObjectArgs);

                        return ImageMetaData.builder()
                                .uuid(UUID.randomUUID().toString())
                                .fileName(objectName)
                                .fileSize(file.getSize())
                                .contentType(file.getContentType())
                                .imageUrl(getPresignedUrl(bucketName, objectName))
                                .build();
                    }).flatMap(imageMetaData ->
                            Mono.fromCallable(() -> {

                                imageRepository.save(imageMetaData);
                                return imageMetaData;
                            }).onErrorResume(e -> {
                                return Mono.empty();
                            })
                    );
                });
    }



    @Override
    public Flux<List<ImageMetaDataDto>> findAllImage(ImageMetaDataDto imageMetaDataDto) {
        List<ImageMetaData> imageMetaData = imageRepository.findAll();
        return Flux.just(imageMapper.toImageMetaDataDtoList(imageMetaData));
    }

    @Override
    public ImageMetaDataDto viewByFileName(String fileName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        ImageMetaData imageDbMetaData = imageRepository.findByFileName(fileName).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Image has not been found!"));

        Path path = Path.of(imageDbMetaData.getFileName());
        StatObjectArgs statObjectArgs = StatObjectArgs.builder()
                .bucket("images")
                .object(path.toString())
                .build();
        StatObjectResponse imageMinioMetaData = minioClient.statObject(statObjectArgs);

        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket("images")
                .object(path.toString())
                .build();

        InputStream inputStream = minioClient.getObject(getObjectArgs);
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

        return ImageMetaDataDto.builder()
                .fileName(imageDbMetaData.getFileName())
                .fileSize(imageDbMetaData.getFileSize())
                .contentType(imageDbMetaData.getContentType())
                .stream(inputStreamResource)
                .build();
    }

    @Override
    public ImageMetaDataDto findImageByUuid(String uuid) {

        ImageMetaData imageMetaData = imageRepository.findByUuid(uuid).orElseThrow(()->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Image has not been found!"));
        return imageMapper.toImageMetaDataDto(imageMetaData);
    }

    @Override
    public void disableImage(String uuid) {
        ImageMetaData imageMetaData = imageRepository.findByUuid(uuid).orElseThrow(()->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Image has not been found!"));
        imageMetaData.setIsDeleted(true);
    }

    @Override
    public void deleteImage(String uuid, String bucketName) {
        String objectName = imageRepository.findByUuid(uuid)
                .orElseThrow().getFileName();
        var found =imageRepository.findByUuid(uuid).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Image has not been found!") );

        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            imageRepository.delete(found);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getPresignedUrl(String bucketName, String fileName) {
        try {
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(fileName)
                    .build();

            return minioClient.getPresignedObjectUrl(args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
