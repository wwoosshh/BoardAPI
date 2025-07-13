package com.example.board.dto;

import com.example.board.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {

    private Long id;
    private String title;
    private String content;
    private String author;
    private Long userId;
    private String username;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    // Entity -> DTO 변환 메서드
    public static PostDto fromEntity(Post post) {
        return PostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getAuthor())
                .userId(post.getUser() != null ? post.getUser().getId() : null)
                .username(post.getUser() != null ? post.getUser().getUsername() : post.getAuthor())
                .categoryId(post.getCategory() != null ? post.getCategory().getId() : null)
                .categoryName(post.getCategory() != null ? post.getCategory().getName() : null)
                .createdDate(post.getCreatedDate())
                .modifiedDate(post.getModifiedDate())
                .build();
    }

    // DTO -> Entity 변환 메서드 (카테고리와 사용자는 서비스에서 설정)
    public Post toEntity() {
        return Post.builder()
                .id(id)
                .title(title)
                .content(content)
                .author(author)
                .build();
    }
}