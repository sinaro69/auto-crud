package com.developerscambodia.media.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtil {

    public static String extractExtension(String originalName) {
        if (originalName.lastIndexOf(".") != -1 && originalName.lastIndexOf(".") != 0) {
            return originalName.substring(originalName.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }

    public static String getVideoDuration(InputStream inputStream, String ffmpegServerPath) throws IOException {

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(ffmpegServerPath, "-i", "-");
            Process process = processBuilder.start();

            // Redirect the video input stream to FFmpeg's input stream
            Thread copyThread = new Thread(() -> {
                try {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        process.getOutputStream().write(buffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    System.out.println("Get duration: " + e.getMessage());
                }
            });
            copyThread.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Duration")) {
                    String[] parts = line.split("Duration: ");
                    int firstComma = parts[1].indexOf(",");
                    return parts[1].substring(0, firstComma);
                }
            }

            reader.close();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.out.println("Get duration: " + e.getMessage());
        }

        return "00:00:00:00";
    }

}
