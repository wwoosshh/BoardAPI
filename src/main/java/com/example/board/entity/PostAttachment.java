// src/main/java/com/example/board/entity/PostAttachment.java
package com.example.board.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_attachments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 저장된 파일명
    @Column(name = "file_name", nullable = false)
    private String fileName;

    // 원본 파일명
    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    // 파일 크기
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    // 파일 타입 (MIME 타입)
    @Column(name = "file_type", nullable = false)
    private String fileType;

    // 파일 종류 (이미지, 비디오, 문서 등)
    @Column(name = "file_category")
    private String fileCategory;

    // 연결된 게시글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // 생성일시
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();

        // 파일 종류 자동 설정
        if (fileType != null) {
            if (fileType.startsWith("image/")) {
                fileCategory = "IMAGE";
            } else if (fileType.startsWith("video/")) {
                fileCategory = "VIDEO";
            } else if (fileType.startsWith("audio/")) {
                fileCategory = "AUDIO";
            } else {
                fileCategory = "OTHER";
            }
        }
    }
}