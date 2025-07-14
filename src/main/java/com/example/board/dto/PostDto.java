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
    private String author;          // 표시용 작성자 (닉네임)
    private Long userId;
    private String username;
    private String userNickname;    // 작성자 닉네임
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    // Entity -> DTO 변환 메서드
    public static PostDto fromEntity(Post post) {
        // 작성자 표시: 사용자가 있으면 닉네임, 없으면 기존 author 필드 사용
        String displayAuthor = post.getAuthor();
        if (post.getUser() != null && post.getUser().getNickname() != null) {
            displayAuthor = post.getUser().getNickname();
        }

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