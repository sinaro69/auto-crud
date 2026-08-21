package com.developerscambodia.media.api.file;

import com.developerscambodia.media.api.file.web.MetaDataDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FileMetaDataMapper {


    FileMetaData toFileMetaData(MetaDataDto metaDataDto);
    MetaDataDto toMetaDataDto(FileMetaData fileMetaData);

    List<MetaDataDto> toMetaDataDtoList(List<FileMetaData> fileMetaDataList);
}
