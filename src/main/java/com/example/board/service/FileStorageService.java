// src/main/java/com/example/board/service/FileStorageService.java (수정)
package com.example.board.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private final Path profileImagesPath;
    private final Path postAttachmentsPath;
    private final Path tempFilesPath;

    @Autowired
    public FileStorageService(FileSystemResource fileStorageResource) {
        this.fileStorageLocation = Paths.get(fileStorageResource.getFile().getAbsolutePath());
        this.profileImagesPath = this.fileStorageLocation.resolve("profiles");
        this.postAttachmentsPath = this.fileStorageLocation.resolve("posts");
        this.tempFilesPath = this.fileStorageLocation.resolve("temp");

        try {
            Files.createDirectories(this.fileStorageLocation);
            Files.createDirectories(this.profileImagesPath);
            Files.createDirectories(this.postAttachmentsPath);
            Files.createDirectories(this.tempFilesPath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * 프로필 이미지 저장
     */
    public String storeProfileImage(MultipartFile file, Long userId) {
        return storeFile(file, profileImagesPath, "profile_" + userId + "_");
    }

    /**
     * 게시글 첨부파일 저장
     */
    public String storePostAttachment(MultipartFile file, Long postId) {
        return storeFile(file, postAttachmentsPath, "post_" + postId + "_");
    }

    /**
     * 임시 파일 저장 (게시글 작성 전)
     */
    public String storeTemporaryFile(MultipartFile file) {
        return storeFile(file, tempFilesPath, "temp_");
    }

    /**
     * 파일 저장 공통 로직
     */
    private String storeFile(MultipartFile file, Path targetPath, String filePrefix) {
        // 파일명 정규화
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            // 파일명에 유효하지 않은 문자가 있는지 확인
            if (originalFilename.contains("..")) {
                throw new RuntimeException("Filename contains invalid path sequence: " + originalFilename);
            }

            // 파일 이름 충돌 방지를 위해 UUID 사용
            String fileExtension = getFileExtension(originalFilename);
            String newFilename = filePrefix + UUID.randomUUID().toString() + fileExtension;

            // 타겟 디렉토리가 없으면 생성
            if (!Files.exists(targetPath)) {
                Files.createDirectories(targetPath);
            }

            // 파일 저장
            Path targetLocation = targetPath.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return newFilename;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFilename, ex);
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename.lastIndexOf(".") != -1 && filename.lastIndexOf(".") != 0) {
            return filename.substring(filename.lastIndexOf("."));
        } else {
            return "";
        }
    }

    /**
     * 프로필 이미지 조회
     */
    public Resource loadProfileImage(String filename) {
        return loadFileAsResource(profileImagesPath.resolve(filename));
    }

    /**
     * 게시글 첨부파일 조회
     */
    public Resource loadPostAttachment(String filename) {
        return loadFileAsResource(postAttachmentsPath.resolve(filename));
    }

    /**
     * 임시 파일 조회
     */
    public Resource loadFileAsResource(String type, String filename) {
        Path basePath;

        switch(type) {
            case "profile":
                basePath = profileImagesPath;
                break;
            case "post":
                basePath = postAttachmentsPath;
                break;
            case "temp":
                basePath = tempFilesPath;
                break;
            default:
                basePath = fileStorageLocation;
        }

        return loadFileAsResource(basePath.resolve(filename));
    }

    /**
     * 파일 로드 공통 로직
     */
    public Resource loadFileAsResource(Path filePath) {
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + filePath);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found: " + filePath, ex);
        }
    }

    /**
     * 파일 삭제
     */
    public boolean deleteFile(String filename, String fileType) {
        try {
            Path filePath;
            if ("profile".equals(fileType)) {
                filePath = profileImagesPath.resolve(filename);
            } else if ("post".equals(fileType)) {
                filePath = postAttachmentsPath.resolve(filename);
            } else if ("temp".equals(fileType)) {
                filePath = tempFilesPath.resolve(filename);
            } else {
                filePath = fileStorageLocation.resolve(filename);
            }

            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }
}