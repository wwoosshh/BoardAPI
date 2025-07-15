// src/main/java/com/example/board/entity/Post.java (업데이트)
package com.example.board.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    // 기존 작성자 이름 필드는 유지 (하위 호환성)
    private String author;

    // 실제 작성자 사용자 참조 추가
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private BoardCategory category;

    // 첨부파일 목록 (Entity로 정의하고 연결)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostAttachment> attachments = new ArrayList<>();

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    // 조회수 필드
    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        modifiedDate = LocalDateTime.now();
        if (viewCount == null) {
            viewCount = 0L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
    }

    // 조회수 증가 메서드
    public void incrementViewCount() {
        this.viewCount = this.viewCount + 1;
    }

    // 첨부파일 추가 메서드
    public void addAttachment(PostAttachment attachment) {
        this.attachments.add(attachment);
        attachment.setPost(this);
    }

    // 첨부파일 제거 메서드
    public void removeAttachment(PostAttachment attachment) {
        this.attachments.remove(attachment);
        attachment.setPost(null);
    }
}