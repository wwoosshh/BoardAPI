// src/main/java/com/example/board/controller/FileController.java (수정)
package com.example.board.controller;

import com.example.board.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    private final FileStorageService fileStorageService;

    @Autowired
    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * 단일 파일 업로드 (임시 저장)
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeTemporaryFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/temp/")
                .path(fileName)
                .toUriString();

        Map<String, Object> response = new HashMap<>();
        response.put("fileName", fileName);
        response.put("originalFileName", file.getOriginalFilename());
        response.put("fileType", file.getContentType());
        response.put("fileSize", file.getSize());
        response.put("fileUrl", fileDownloadUri);

        return ResponseEntity.ok(response);
    }

    /**
     * 다중 파일 업로드 (임시 저장)
     */
    @PostMapping("/uploads")
    public ResponseEntity<?> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        List<Map<String, Object>> responses = Arrays.stream(files)
                .map(file -> {
                    String fileName = fileStorageService.storeTemporaryFile(file);
                    String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/api/files/temp/")
                            .path(fileName)
                            .toUriString();

                    Map<String, Object> response = new HashMap<>();
                    response.put("fileName", fileName);
                    response.put("originalFileName", file.getOriginalFilename());
                    response.put("fileType", file.getContentType());
                    response.put("fileSize", file.getSize());
                    response.put("fileUrl", fileDownloadUri);
                    return response;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 프로필 이미지 조회
     */
    @GetMapping("/profiles/{fileName:.+}")
    public ResponseEntity<Resource> getProfileImage(@PathVariable String fileName) {
        return getFileResource(fileStorageService.loadProfileImage(fileName));
    }

    /**
     * 게시글 첨부파일 조회
     */
    @GetMapping("/posts/{fileName:.+}")
    public ResponseEntity<Resource> getPostAttachment(@PathVariable String fileName) {
        return getFileResource(fileStorageService.loadPostAttachment(fileName));
    }

    /**
     * 임시 파일 조회
     */
    @GetMapping("/temp/{fileName:.+}")
    public ResponseEntity<Resource> getTempFile(@PathVariable String fileName) {
        // 수정된 부분: 문자열을 Path로 변환하는 loadFileAsResource 메서드 호출 수정
        Resource resource = fileStorageService.loadFileAsResource("temp", fileName);
        return getFileResource(resource);
    }

    /**
     * 파일 리소스 반환 공통 로직
     */
    private ResponseEntity<Resource> getFileResource(Resource resource) {
        String contentType = "application/octet-stream";
        try {
            contentType = determineContentType(resource.getFilename());
        } catch (Exception e) {
            // 기본 타입 사용
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * 파일 확장자로 Content-Type 결정
     */
    private String determineContentType(String filename) {
        if (filename == null) return "application/octet-stream";

        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFilename.endsWith(".png")) {
            return "image/png";
        } else if (lowerFilename.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFilename.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerFilename.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerFilename.endsWith(".mp4")) {
            return "video/mp4";
        } else if (lowerFilename.endsWith(".mov")) {
            return "video/quicktime";
        } else if (lowerFilename.endsWith(".webm")) {
            return "video/webm";
        } else {
            return "application/octet-stream";
        }
    }

    /**
     * 파일 삭제
     */
    @DeleteMapping("/{fileType}/{fileName:.+}")
    public ResponseEntity<?> deleteFile(
            @PathVariable String fileType,
            @PathVariable String fileName) {

        boolean deleted = fileStorageService.deleteFile(fileName, fileType);

        Map<String, Object> response = new HashMap<>();
        response.put("fileName", fileName);
        response.put("deleted", deleted);

        return ResponseEntity.ok(response);
    }
}