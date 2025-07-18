// src/main/java/com/example/board/entity/User.java (업데이트)
package com.example.board.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    private String name;

    // 닉네임 필드
    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    // 프로필 이미지 필드 추가
    @Column(name = "profile_image")
    private String profileImage;

    // 자기소개 필드 추가
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    // 웹사이트 URL 필드 추가
    @Column(name = "website")
    private String website;

    // 소셜 미디어 링크 필드 추가
    @Column(name = "social_links")
    private String socialLinks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_managed_categories",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<BoardCategory> managedCategories = new HashSet<>();

    private boolean enabled = true;

    private boolean locked = false;

    private int warningCount = 0;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        if (role == null) {
            role = UserRole.ROLE_USER;
        }
        // 닉네임이 없으면 이름으로 설정
        if (nickname == null || nickname.trim().isEmpty()) {
            nickname = name != null ? name : username;
        }
        // 기본값 강제 설정
        enabled = true;
        locked = false;
        warningCount = 0;
    }

    // 관리자 회원인지 확인하는 메서드
    public boolean isModeratorFor(BoardCategory category) {
        return role == UserRole.ROLE_MODERATOR && managedCategories.contains(category);
    }

    // 관리자인지 확인하는 메서드
    public boolean isAdmin() {
        return role == UserRole.ROLE_ADMIN;
    }
}