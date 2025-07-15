// src/main/java/com/example/board/dto/UserDto.java (업데이트)
package com.example.board.dto;

import com.example.board.entity.User;
import com.example.board.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;
    private String username;
    private String email;
    private String name;
    private String nickname;
    private UserRole role;

    // 프로필 관련 필드 추가
    private String profileImage;
    private String bio;
    private String website;
    private String socialLinks;

    @Builder.Default
    private Set<Long> managedCategoryIds = new HashSet<>();
    private boolean enabled;
    private boolean locked;
    private int warningCount;
    private LocalDateTime createdDate;

    public static UserDto fromEntity(User user) {
        // 실제 관리 게시판 ID들 추출
        Set<Long> categoryIds = new HashSet<>();
        if (user.getManagedCategories() != null && !user.getManagedCategories().isEmpty()) {
            categoryIds = user.getManagedCategories().stream()
                    .map(category -> category.getId())
                    .collect(Collectors.toSet());
        }

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .bio(user.getBio())
                .website(user.getWebsite())
                .socialLinks(user.getSocialLinks())
                .role(user.getRole())
                .managedCategoryIds(categoryIds)
                .enabled(user.isEnabled())
                .locked(user.isLocked())
                .warningCount(user.getWarningCount())
                .createdDate(user.getCreatedDate())
                .build();
    }

    // 프로필 정보 업데이트를 위한 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileUpdateRequest {
        private String nickname;
        private String name;
        private String bio;
        private String website;
        private String socialLinks;
    }
}