// src/main/java/com/example/board/controller/ProfileController.java
package com.example.board.controller;

import com.example.board.dto.UserDto;
import com.example.board.entity.User;
import com.example.board.repository.UserRepository;
import com.example.board.service.FileStorageService;
import com.example.board.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class ProfileController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public ProfileController(UserService userService, UserRepository userRepository, FileStorageService fileStorageService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * 현재 사용자의 프로필 조회
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "인증이 필요합니다."));
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        UserDto userDto = UserDto.fromEntity(user);

        // 프로필 이미지 URL 설정
        if (userDto.getProfileImage() != null) {
            String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/profiles/")
                    .path(userDto.getProfileImage())
                    .toUriString();

            // DTO에는 없으므로 Map으로 반환
            Map<String, Object> response = new HashMap<>();
            response.put("user", userDto);
            response.put("profileImageUrl", imageUrl);

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.ok(userDto);
    }

    /**
     * 특정 사용자의 프로필 조회
     */
    @GetMapping("/{username}")
    public ResponseEntity<?> getUserProfile(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        UserDto userDto = UserDto.fromEntity(user);

        // 민감한 정보 제거
        userDto.setEmail(null);

        // 프로필 이미지 URL 설정
        if (userDto.getProfileImage() != null) {
            String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/profiles/")
                    .path(userDto.getProfileImage())
                    .toUriString();

            Map<String, Object> response = new HashMap<>();
            response.put("user", userDto);
            response.put("profileImageUrl", imageUrl);

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.ok(userDto);
    }

    /**
     * 프로필 정보 업데이트
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody UserDto.ProfileUpdateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "인증이 필요합니다."));
        }

        try {
            UserDto updatedUser = userService.updateProfile(auth.getName(), request);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 프로필 이미지 업로드
     */
    @PostMapping("/image")
    public ResponseEntity<?> uploadProfileImage(@RequestParam("image") MultipartFile file) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "인증이 필요합니다."));
        }

        try {
            User user = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // 기존 프로필 이미지가 있으면 삭제
            if (user.getProfileImage() != null) {
                fileStorageService.deleteFile(user.getProfileImage(), "profile");
            }

            // 새 이미지 저장
            String fileName = fileStorageService.storeProfileImage(file, user.getId());
            user.setProfileImage(fileName);
            userRepository.save(user);

            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/profiles/")
                    .path(fileName)
                    .toUriString();

            Map<String, Object> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("fileUrl", fileUrl);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}