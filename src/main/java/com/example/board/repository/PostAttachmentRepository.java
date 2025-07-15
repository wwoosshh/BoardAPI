// src/main/java/com/example/board/repository/PostAttachmentRepository.java
package com.example.board.repository;

import com.example.board.entity.Post;
import com.example.board.entity.PostAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostAttachmentRepository extends JpaRepository<PostAttachment, Long> {
    List<PostAttachment> findByPost(Post post);
    void deleteByPost(Post post);
}