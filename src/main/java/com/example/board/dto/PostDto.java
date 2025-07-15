// src/main/java/com/example/board/dto/PostDto.java (업데이트)
package com.example.board.dto;

import com.example.board.entity.Post;
import com.example.board.entity.PostAttachment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {

    private Long id;
    private String title;
    private String content;
    private String author;          // 표시용 작성자 (닉네임)
    private Long userId;
    private String username;
    private String userNickname;    // 작성자 닉네임
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private Long viewCount;         // 조회수 필드 추가

    // 첨부파일 목록 추가
    @Builder.Default
    private List<AttachmentDto> attachments = new ArrayList<>();

    // Entity -> DTO 변환 메서드
    public static PostDto fromEntity(Post post) {
        // 작성자 표시: 사용자가 있으면 닉네임, 없으면 기존 author 필드 사용
        String displayAuthor = post.getAuthor();
        if (post.getUser() != null && post.getUser().getNickname() != null) {
            displayAuthor = post.getUser().getNickname();
        }

        // 첨부파일 변환
        List<AttachmentDto> attachmentDtos = post.getAttachments().stream()
                .map(AttachmentDto::fromEntity)
                .collect(Collectors.toList());

        return PostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(displayAuthor)  // 닉네임 또는 기존 author
                .userId(post.getUser() != null ? post.getUser().getId() : null)
                .username(post.getUser() != null ? post.getUser().getUsername() : null)
                .userNickname(post.getUser() != null ? post.getUser().getNickname() : null)
                .categoryId(post.getCategory() != null ? post.getCategory().getId() : null)
                .categoryName(post.getCategory() != null ? post.getCategory().getName() : null)
                .createdDate(post.getCreatedDate())
                .modifiedDate(post.getModifiedDate())
                .viewCount(post.getViewCount())
                .attachments(attachmentDtos)
                .build();
    }

    // DTO -> Entity 변환 메서드 (카테고리와 사용자는 서비스에서 설정)
    public Post toEntity() {
        return Post.builder()
                .id(id)
                .title(title)
                .content(content)
                .author(author)
                .viewCount(viewCount != null ? viewCount : 0L)
                .build();
    }

    // 첨부파일 DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentDto {
        private Long id;
        private String fileName;
        private String originalFileName;
        private Long fileSize;
        private String fileType;
        private String fileCategory;
        private LocalDateTime createdDate;

        // URL 생성 (프론트엔드에서 사용)
        private String fileUrl;

        // 썸네일 URL (이미지인 경우)
        private String thumbnailUrl;

        public static AttachmentDto fromEntity(PostAttachment attachment) {
            return AttachmentDto.builder()
                    .id(attachment.getId())
                    .fileName(attachment.getFileName())
                    .originalFileName(attachment.getOriginalFileName())
                    .fileSize(attachment.getFileSize())
                    .fileType(attachment.getFileType())
                    .fileCategory(attachment.getFileCategory())
                    .createdDate(attachment.getCreatedDate())
                    .fileUrl("/api/files/posts/" + attachment.getFileName())
                    .thumbnailUrl(attachment.getFileCategory().equals("IMAGE") ?
                            "/api/files/posts/" + attachment.getFileName() + "?thumbnail=true" : null)
                    .build();
        }
    }
}