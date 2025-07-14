package com.example.board.dto;

import com.example.board.entity.Comment;
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
public class CommentDto {

    private Long id;
    private String content;
    private Long userId;
    private String userNickname;
    private String username;
    private Long postId;
    private Long parentId;
    private boolean deleted;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    // 대댓글 목록 (계층 구조)
    @Builder.Default
    private List<CommentDto> children = new ArrayList<>();

    // Entity -> DTO 변환 (자식 댓글 포함)
    public static CommentDto fromEntity(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent())
                .userId(comment.getUser() != null ? comment.getUser().getId() : null)
                .userNickname(comment.getUser() != null ? comment.getUser().getNickname() : "탈퇴한 사용자")
                .username(comment.getUser() != null ? comment.getUser().getUsername() : "unknown")
                .postId(comment.getPost() != null ? comment.getPost().getId() : null)
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .deleted(comment.isDeleted())
                .createdDate(comment.getCreatedDate())
                .modifiedDate(comment.getModifiedDate())
                .children(comment.getChildren().stream()
                        .filter(child -> !child.isDeleted()) // 삭제되지 않은 자식만
                        .map(CommentDto::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }

    // Entity -> DTO 변환 (자식 댓글 제외 - 단순 버전)
    public static CommentDto fromEntitySimple(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent())
                .userId(comment.getUser() != null ? comment.getUser().getId() : null)
                .userNickname(comment.getUser() != null ? comment.getUser().getNickname() : "탈퇴한 사용자")
                .username(comment.getUser() != null ? comment.getUser().getUsername() : "unknown")
                .postId(comment.getPost() != null ? comment.getPost().getId() : null)
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .deleted(comment.isDeleted())
                .createdDate(comment.getCreatedDate())
                .modifiedDate(comment.getModifiedDate())
                .build();
    }

    // 댓글 생성 요청 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private String content;
        private Long parentId; // null이면 최상위 댓글, 값이 있으면 대댓글
    }

    // 댓글 수정 요청 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String content;
    }
}