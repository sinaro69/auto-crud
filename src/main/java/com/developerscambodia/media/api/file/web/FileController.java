package com.developerscambodia.media.api.file.web;

import com.developerscambodia.media.api.file.FileMetaData;
import com.developerscambodia.media.api.file.FileService;
import com.developerscambodia.media.base.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping(value = "/files",produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private static final String ALLOWED_EXTENSIONS = "pdf";

    @PostMapping(value = {"/",""}, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Mono<BaseResponse<Object>> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {

        if (!file.isEmpty() && ALLOWED_EXTENSIONS.contains(file.getContentType().split("/")[1])){
            try {
                FileMetaData metaData = fileService.uploadFile(file, "image-upload").block();
                return Mono.just(BaseResponse.builder().code(HttpStatus.CREATED.value())
                        .message("File uploaded successfully")
                        .timestamp(LocalDateTime.now())
                        .data(metaData).build());

            } catch (Exception e) {
                return Mono.just(BaseResponse.builder()
                        .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message("Failed to upload file!!!").build());
            }

        } else {
            return Mono.just(BaseResponse.builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message("Invalid file")
                    .timestamp(LocalDateTime.now())
                    .build());
        }

    }
    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<?>> uploadMultipleFiles(@RequestPart("files") List<MultipartFile> files) {
        Flux<FileMetaData> uploadedFiles = fileService.uploadMultipleFiles(files, "file-upload");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.builder()
                        .code(HttpStatus.CREATED.value())
                        .message("Files uploaded successfully.")
                        .timestamp(LocalDateTime.now())
                        .data(uploadedFiles.collectList().block()) // Collect the Flux into a List<ImageMetaData>
                        .build()
                );
    }
    @GetMapping(value = {"/",""})
    public Flux<BaseResponse<?>> findAllFiles(MetaDataDto metaDataDto) {
        return fileService.findAllFiles(metaDataDto)
                .map(fileMetaDataList -> BaseResponse.builder()
                        .code(HttpStatus.OK.value())
                        .message("Files has been found.")
                        .timestamp(LocalDateTime.now())
                        .data(fileMetaDataList)
                        .build()
                );
    }
    @GetMapping("/file-name/{fileName}")
    public BaseResponse<Object> findFileByName(@PathVariable String fileName){
        MetaDataDto file =fileService.findFileByFileName(fileName);

        return BaseResponse.builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("File has been found.")
                .timestamp(LocalDateTime.now())
                .data(file)
                .build();
    }

    @GetMapping("/{uuid}")
    public BaseResponse<Object> findFileByUuid(@PathVariable String uuid){
        MetaDataDto fileBuyUuid =fileService.findFileByUuid(uuid);
        return BaseResponse.builder()
                .code(HttpStatus.OK.value())
                .message("Image has been found.")
                .timestamp(LocalDateTime.now())
                .data(fileBuyUuid)
                .build();
    }

    @PutMapping("/{uuid}/disable")
    public ResponseEntity<Object> disableFile(@PathVariable String uuid) {
        fileService.disableFile(uuid);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Object> deleteFile(@PathVariable String uuid,Boolean status) {
        fileService.deleteFile(uuid,status, "video-upload");
        return ResponseEntity.ok().build();
    }

}
