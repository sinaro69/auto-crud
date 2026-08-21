package com.developerscambodia.media.api.video;

import com.developerscambodia.media.api.video.web.VideoMetaDataDto;
import com.developerscambodia.media.utils.StreamUtil;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Mapper(componentModel = "spring")
@RequiredArgsConstructor
public abstract class VideoMapper {

    StreamUtil streamUtil;

    @Autowired
    public void setStreamUtil(StreamUtil streamUtil) {
        this.streamUtil = streamUtil;
    }

    String getUri() {
        return streamUtil.build();
    }

    @Mapping(target = "streamUri", expression = "java(getUri() + videoMetaData.getFileName())")
    abstract VideoMetaDataDto toVideoDto(VideoMetaData videoMetaData);

    abstract VideoMetaData toVideoMetaData(VideoMetaDataDto videoMetaDataDto);

    abstract List<VideoMetaDataDto> toVideoMetaDataDtoList(List<VideoMetaData> videoMetaDataList);
}
