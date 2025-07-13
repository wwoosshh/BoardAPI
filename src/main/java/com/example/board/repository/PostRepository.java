package com.example.board.repository;

import com.example.board.entity.BoardCategory;
import com.example.board.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedDateDesc();
    List<Post> findByCategoryOrderByCreatedDateDesc(BoardCategory category);
}