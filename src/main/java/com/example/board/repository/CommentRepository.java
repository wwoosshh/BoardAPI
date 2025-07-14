package com.example.board.repository;

import com.example.board.entity.Comment;
import com.example.board.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 게시글의 최상위 댓글들만 조회 (부모가 없는 댓글들)
    List<Comment> findByPostAndParentIsNullAndDeletedFalseOrderByCreatedDateAsc(Post post);

    // 특정 게시글의 모든 댓글 조회 (삭제되지 않은 것들만)
    List<Comment> findByPostAndDeletedFalseOrderByCreatedDateAsc(Post post);

    // 특정 댓글의 대댓글들 조회
    List<Comment> findByParentAndDeletedFalseOrderByCreatedDateAsc(Comment parent);

    // 특정 게시글의 댓글 수 조회 (삭제되지 않은 것들만)
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post = :post AND c.deleted = false")
    long countByPostAndDeletedFalse(@Param("post") Post post);

    // 특정 게시글과 그 모든 댓글들을 계층적으로 조회
    @Query("SELECT c FROM Comment c WHERE c.post = :post AND c.deleted = false ORDER BY " +
            "CASE WHEN c.parent IS NULL THEN c.id ELSE c.parent.id END, " +
            "c.parent.id ASC NULLS FIRST, c.createdDate ASC")
    List<Comment> findByPostOrderByHierarchy(@Param("post") Post post);
}