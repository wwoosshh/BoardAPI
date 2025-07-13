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
    private UserRole role;
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
                .role(user.getRole())
                .managedCategoryIds(categoryIds)  // 실제 관리 게시판 ID들
                .enabled(user.isEnabled())
                .locked(user.isLocked())
                .warningCount(user.getWarningCount())
                .createdDate(user.getCreatedDate())
                .build();
    }
}