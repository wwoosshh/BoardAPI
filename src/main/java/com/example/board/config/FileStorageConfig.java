// src/main/java/com/example/board/config/FileStorageConfig.java
package com.example.board.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.IOException;

@Configuration
public class FileStorageConfig {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Bean
    public FileSystemResource fileSystemResource() {
        try {
            File uploadDirectory = new File(uploadDir);
            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();

                // 프로필 이미지, 게시글 첨부파일 디렉토리 생성
                new File(uploadDirectory, "profiles").mkdirs();
                new File(uploadDirectory, "posts").mkdirs();
            }

            return new FileSystemResource(uploadDirectory);
        } catch (Exception e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }
}