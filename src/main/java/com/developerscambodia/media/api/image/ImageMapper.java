package com.developerscambodia.media.api.image;

import com.developerscambodia.media.api.image.web.ImageMetaDataDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ImageMapper {

    ImageMetaData toImageMetaData(ImageMetaDataDto imageMetaDataDto);
    ImageMetaDataDto toImageMetaDataDto(ImageMetaData imageMetaData);

    List<ImageMetaDataDto> toImageMetaDataDtoList(List<ImageMetaData> imageMetaDataList);
}
