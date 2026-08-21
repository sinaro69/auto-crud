package com.developerscambodia.media.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StreamUtil {

    @Value("${stream.uri}")
    private String streamUri;

    public String build() {
        System.out.println("STREAM URI: " + streamUri);
        return streamUri;
    }

}
