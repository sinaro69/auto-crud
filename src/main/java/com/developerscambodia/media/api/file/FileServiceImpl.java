package com.developerscambodia.media.api.file;

import com.developerscambodia.media.api.image.ImageService;
import com.developerscambodia.media.api.file.web.MetaDataDto;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FileServiceImpl implements FileService {
    private final MinioClient minioClient;
    private final FileRepository fileRepository;
    private final ImageService imageService;

    private final FileMetaDataMapper metaDataMapper;


    @Override
    public Mono<FileMetaData> uploadFile(MultipartFile file, String bucketName) throws IOException {
        String uniqueIdentifier = UUID.randomUUID().toString();
        String objectName = uniqueIdentifier + "_" + file.getOriginalFilename();
        String url = imageService.getPresignedUrl(bucketName, objectName);

        return Mono.fromCallable(() -> {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .build();

            minioClient.putObject(putObjectArgs);


            return FileMetaData.builder()
                    .uuid(UUID.randomUUID().toString())
                    .fileName(objectName)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .fileUrl(url)
                    .build();

        }).flatMap(FileMetaData -> {
            return Mono.defer(() -> {
               try {
                   // Simulating the non-reactive save operation
                   fileRepository.save(FileMetaData);
                   return Mono.just(FileMetaData);
               } catch (Exception e) {
                   // Handle the exception gracefully, maybe log the error
                   e.printStackTrace();
                   return Mono.empty();
               }
           });
                }
        );


    }

    @Override
    public Flux<FileMetaData> uploadMultipleFiles(List<MultipartFile> files, String bucketName) {
        return Flux.fromIterable(files)
                .flatMap(file -> {
                    String uniqueIdentifier = UUID.randomUUID().toString();
                    String objectName = uniqueIdentifier + "_" + file.getOriginalFilename();
                    String url = imageService.getPresignedUrl(bucketName, objectName);

                    return Mono.fromCallable(() -> {
                        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(file.getInputStream(), file.getSize(), -1)
                                .build();
                        minioClient.putObject(putObjectArgs);

                        return FileMetaData.builder()
                                .uuid(UUID.randomUUID().toString())
                                .fileName(objectName)
                                .fileSize(file.getSize())
                                .contentType(file.getContentType())
                                .fileUrl(url)
                                .build();
                    }).flatMap(fileMetaData ->
                            Mono.fromCallable(() -> {

                                fileRepository.save(fileMetaData);
                                return fileMetaData;
                            }).onErrorResume(e -> {
                                e.printStackTrace();
                                return Mono.empty();
                            })
                    );
                });
    }

    @Override
    public Flux<List<MetaDataDto>> findAllFiles(MetaDataDto MetaDataDto) {
        List<FileMetaData> fileMetaData = fileRepository.findAll();
        return Flux.just(metaDataMapper.toMetaDataDtoList(fileMetaData));
    }

    @Override
    public MetaDataDto findFileByFileName(String fileName) {
        FileMetaData fileMetaData = fileRepository.findByFileName(fileName);
        return metaDataMapper.toMetaDataDto(fileMetaData);
    }

    @Override
    public MetaDataDto findFileByUuid(String uuid) {
        FileMetaData fileMetaData = fileRepository.findByUuid(uuid);
        return metaDataMapper.toMetaDataDto(fileMetaData);
    }


    @Override
    public void disableFile(String uuid) {
        FileMetaData fileMetaData = fileRepository.findByUuid(uuid);
        fileMetaData.setIsDeleted(true);
        fileRepository.save(fileMetaData);
    }

    @Override
    public void deleteFile(String uuid,Boolean status, String bucketName) {
        String objectName = fileRepository.findByUuid(uuid).getFileName();

        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
//            fileRepository.findByUuidAndIsDeleted(uuid,isDeleted);
            fileRepository.delete(fileRepository.findByUuid(uuid));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
