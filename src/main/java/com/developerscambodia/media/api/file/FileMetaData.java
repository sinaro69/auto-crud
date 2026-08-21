package com.developerscambodia.media.api.file;

import com.developerscambodia.media.base.BasedEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class FileMetaData extends BasedEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String uuid;
    private String fileName;
    private Long fileSize;
    private String contentType;
    @Column(columnDefinition = "VARCHAR(10000)", length = 10000)
    private String fileUrl;

    
}
