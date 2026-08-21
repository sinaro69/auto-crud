package com.developerscambodia.media.api.file;



import com.developerscambodia.media.api.file.web.MetaDataDto;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;


public interface FileService {

    Mono<FileMetaData> uploadFile(MultipartFile file, String bucketName) throws IOException;

    Flux<FileMetaData> uploadMultipleFiles(List<MultipartFile> files, String bucketName);
    Flux<List<MetaDataDto>> findAllFiles(MetaDataDto MetaDataDto);
    MetaDataDto findFileByFileName(String fileName);
    MetaDataDto findFileByUuid(String uuid);
    void disableFile(String uuid);
    @Transactional
    void deleteFile(String uuid,Boolean status, String bucketName);


}
