package com.example.board.service;

import com.example.board.dto.CommentDto;
import com.example.board.entity.Comment;
import com.example.board.entity.Post;
import com.example.board.entity.User;
import com.example.board.entity.UserRole;
import com.example.board.repository.CommentRepository;
import com.example.board.repository.PostRepository;
import com.example.board.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository,
                          PostRepository postRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    // 특정 게시글의 댓글 목록 조회 (계층 구조)
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다: " + postId));

        // 최상위 댓글들만 조회
        List<Comment> topLevelComments = commentRepository
                .findByPostAndParentIsNullAndDeletedFalseOrderByCreatedDateAsc(post);

        return topLevelComments.stream()
                .map(CommentDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 댓글 생성
    @Transactional
    public CommentDto createComment(Long postId, CommentDto.CreateRequest request) {
        // 현재 사용자 가져오기
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        // 게시글 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다: " + postId));

        // 부모 댓글 확인 (대댓글인 경우)
        Comment parentComment = null;
        if (request.getParentId() != null) {
            parentComment = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("부모 댓글을 찾을 수 없습니다: " + request.getParentId()));

            // 부모 댓글이 같은 게시글에 속하는지 확인
            if (!parentComment.getPost().getId().equals(postId)) {
                throw new RuntimeException("부모 댓글이 해당 게시글에 속하지 않습니다.");
            }
        }

        // 댓글 생성
        Comment comment = Comment.builder()
                .content(request.getContent())
                .user(currentUser)
                .post(post)
                .parent(parentComment)
                .deleted(false)
                .build();

        Comment savedComment = commentRepository.save(comment);

        System.out.println("💬 댓글 작성 완료: " + currentUser.getNickname() +
                (parentComment != null ? " (대댓글)" : " (댓글)"));

        return CommentDto.fromEntitySimple(savedComment);
    }

    // 댓글 수정
    @Transactional
    public CommentDto updateComment(Long commentId, CommentDto.UpdateRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다: " + commentId));

        User currentUser = getCurrentUser();
        if (!isAllowedToModifyComment(currentUser, comment)) {
            throw new RuntimeException("댓글을 수정할 권한이 없습니다.");
        }

        if (comment.isDeleted()) {
            throw new RuntimeException("삭제된 댓글은 수정할 수 없습니다.");
        }

        comment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);

        System.out.println("✏️ 댓글 수정 완료: " + currentUser.getNickname());

        return CommentDto.fromEntitySimple(updatedComment);
    }

    // 댓글 삭제 (소프트 삭제)
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다: " + commentId));

        User currentUser = getCurrentUser();
        if (!isAllowedToModifyComment(currentUser, comment)) {
            throw new RuntimeException("댓글을 삭제할 권한이 없습니다.");
        }

        // 소프트 삭제 처리
        comment.setDeleted(true);
        comment.setContent(""); // 내용 삭제
        commentRepository.save(comment);

        System.out.println("🗑️ 댓글 삭제 완료: " + currentUser.getNickname());
    }

    // 댓글 수 조회
    @Transactional(readOnly = true)
    public long getCommentCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다: " + postId));

        return commentRepository.countByPostAndDeletedFalse(post);
    }

    // 현재 사용자 가져오기
    private User getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() &&
                    !auth.getPrincipal().equals("anonymousUser")) {
                return userRepository.findByUsername(auth.getName()).orElse(null);
            }
        } catch (Exception e) {
            System.out.println("⚠️ 사용자 인증 정보 확인 실패: " + e.getMessage());
        }
        return null;
    }

    // 댓글 수정/삭제 권한 확인
    private boolean isAllowedToModifyComment(User user, Comment comment) {
        if (user == null) return false;

        // 매니저는 모든 댓글 수정/삭제 가능
        if (user.getRole() == UserRole.ROLE_MANAGER) {
            return true;
        }

        // 관리자는 모든 댓글 수정/삭제 가능
        if (user.getRole() == UserRole.ROLE_ADMIN) {
            return true;
        }

        // 관리자회원은 모든 댓글 수정/삭제 가능
        if (user.getRole() == UserRole.ROLE_MODERATOR) {
            return true;
        }

        // 본인 댓글은 수정/삭제 가능
        return comment.getUser() != null && comment.getUser().getId().equals(user.getId());
    }
}