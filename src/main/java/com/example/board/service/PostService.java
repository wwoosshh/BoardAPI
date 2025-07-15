// src/main/java/com/example/board/service/PostService.java (업데이트)
package com.example.board.service;

import com.example.board.dto.PostDto;
import com.example.board.entity.BoardCategory;
import com.example.board.entity.Post;
import com.example.board.entity.PostAttachment;
import com.example.board.entity.User;
import com.example.board.entity.UserRole;
import com.example.board.repository.BoardCategoryRepository;
import com.example.board.repository.PostAttachmentRepository;
import com.example.board.repository.PostRepository;
import com.example.board.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final BoardCategoryRepository boardCategoryRepository;
    private final UserRepository userRepository;
    private final PostAttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public PostService(PostRepository postRepository,
                       BoardCategoryRepository boardCategoryRepository,
                       UserRepository userRepository,
                       PostAttachmentRepository attachmentRepository,
                       FileStorageService fileStorageService) {
        this.postRepository = postRepository;
        this.boardCategoryRepository = boardCategoryRepository;
        this.userRepository = userRepository;
        this.attachmentRepository = attachmentRepository;
        this.fileStorageService = fileStorageService;
    }

    // 모든 게시글 목록 조회
    @Transactional(readOnly = true)
    public List<PostDto> getAllPosts() {
        return postRepository.findAllByOrderByCreatedDateDesc()
                .stream()
                .map(PostDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 카테고리별 게시글 목록 조회
    @Transactional(readOnly = true)
    public List<PostDto> getPostsByCategory(Long categoryId) {
        BoardCategory category = boardCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다: " + categoryId));

        return postRepository.findByCategoryOrderByCreatedDateDesc(category)
                .stream()
                .map(PostDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 게시글 상세 조회 (조회수 증가 추가)
    @Transactional
    public PostDto getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다: " + id));

        // 조회수 증가
        post.incrementViewCount();
        post = postRepository.save(post);

        return PostDto.fromEntity(post);
    }

    // 게시글 생성 (첨부파일 처리 추가)
    @Transactional
    public PostDto createPost(PostDto postDto, List<MultipartFile> files) {
        User user = null;

        // 현재 인증된 사용자 가져오기
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() &&
                    !auth.getPrincipal().equals("anonymousUser")) {
                user = userRepository.findByUsername(auth.getName()).orElse(null);
                if (user != null) {
                    System.out.println("✍️ 게시글 작성자: " + user.getUsername() +
                            " (닉네임: " + user.getNickname() + ", 권한: " + user.getRole() + ")");
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ 인증 정보 확인 실패: " + e.getMessage());
        }

        // 카테고리 확인
        BoardCategory category = null;
        if (postDto.getCategoryId() != null) {
            category = boardCategoryRepository.findById(postDto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다: " + postDto.getCategoryId()));
            System.out.println("📋 게시판: " + category.getName());
        }

        // 작성자 설정: 닉네임 우선 사용
        String authorName = "익명";
        if (user != null) {
            authorName = user.getNickname() != null ? user.getNickname() :
                    (user.getName() != null ? user.getName() : user.getUsername());
        }

        Post post = Post.builder()
                .title(postDto.getTitle())
                .content(postDto.getContent())
                .author(authorName)  // 닉네임 사용
                .user(user)
                .category(category)
                .viewCount(0L)
                .attachments(new ArrayList<>()) // 첨부파일 초기화
                .build();

        Post savedPost = postRepository.save(post);

        // 첨부파일 처리
        if (files != null && !files.isEmpty()) {
            List<PostAttachment> attachments = saveAttachments(files, savedPost);
            savedPost.setAttachments(attachments);
            savedPost = postRepository.save(savedPost);
        }

        System.out.println("✅ 게시글 작성 완료: " + savedPost.getTitle() + " (작성자: " + authorName + ")");

        return PostDto.fromEntity(savedPost);
    }

    // 첨부파일 저장 메소드
    private List<PostAttachment> saveAttachments(List<MultipartFile> files, Post post) {
        List<PostAttachment> attachments = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String fileName = fileStorageService.storePostAttachment(file, post.getId());

            PostAttachment attachment = PostAttachment.builder()
                    .fileName(fileName)
                    .originalFileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .fileType(file.getContentType())
                    .post(post)
                    .build();

            attachments.add(attachmentRepository.save(attachment));
        }

        return attachments;
    }

    // 게시글 수정 (첨부파일 처리 추가)
    @Transactional
    public PostDto updatePost(Long id, PostDto postDto, List<MultipartFile> newFiles, List<Long> filesToDelete) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다: " + id));

        User currentUser = null;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() &&
                    !auth.getPrincipal().equals("anonymousUser")) {
                currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
            }
        } catch (Exception e) {
            // 인증 정보가 없어도 계속 진행 (임시 개발 환경)
        }

        // 권한 체크
        if (currentUser != null && !isAllowedToModify(currentUser, post)) {
            throw new RuntimeException("게시글을 수정할 권한이 없습니다");
        }

        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());

        // 카테고리 변경
        if (postDto.getCategoryId() != null) {
            BoardCategory category = boardCategoryRepository.findById(postDto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다: " + postDto.getCategoryId()));
            post.setCategory(category);
        }

        // 삭제할 첨부파일 처리
        if (filesToDelete != null && !filesToDelete.isEmpty()) {
            for (Long fileId : filesToDelete) {
                post.getAttachments().stream()
                        .filter(a -> a.getId().equals(fileId))
                        .findFirst()
                        .ifPresent(attachment -> {
                            // 파일 시스템에서 삭제
                            fileStorageService.deleteFile(attachment.getFileName(), "post");
                            // 엔티티에서 삭제
                            post.getAttachments().remove(attachment);
                            // DB에서 삭제
                            attachmentRepository.delete(attachment);
                        });
            }
        }

        // 새 첨부파일 추가
        if (newFiles != null && !newFiles.isEmpty()) {
            List<PostAttachment> newAttachments = saveAttachments(newFiles, post);
            post.getAttachments().addAll(newAttachments);
        }

        Post updatedPost = postRepository.save(post);
        return PostDto.fromEntity(updatedPost);
    }

    // 게시글 삭제 (첨부파일 삭제 추가)
    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다: " + id));

        User currentUser = null;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() &&
                    !auth.getPrincipal().equals("anonymousUser")) {
                currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
            }
        } catch (Exception e) {
            // 인증 정보가 없어도 계속 진행 (임시 개발 환경)
        }

        // 권한 체크
        if (currentUser != null && !isAllowedToModify(currentUser, post)) {
            throw new RuntimeException("게시글을 삭제할 권한이 없습니다");
        }

        // 첨부파일 삭제
        for (PostAttachment attachment : post.getAttachments()) {
            fileStorageService.deleteFile(attachment.getFileName(), "post");
        }

        // 게시글 삭제
        postRepository.delete(post);
    }

    // 게시글 수정/삭제 권한 체크 (권한 체계 수정)
    private boolean isAllowedToModify(User user, Post post) {
        // 🏢 매니저는 모든 글을 수정/삭제 가능
        if (user.getRole() == UserRole.ROLE_MANAGER) {
            System.out.println("👑 매니저 권한으로 수정/삭제 허용: " + user.getUsername());
            return true;
        }

        // 🔑 관리자는 모든 글을 수정/삭제 가능
        if (user.getRole() == UserRole.ROLE_ADMIN) {
            System.out.println("🔑 관리자 권한으로 수정/삭제 허용: " + user.getUsername());
            return true;
        }

        // 🛡️ 관리자회원은 모든 글을 수정/삭제 가능
        if (user.getRole() == UserRole.ROLE_MODERATOR) {
            System.out.println("🛡️ 관리자회원 권한으로 수정/삭제 허용: " + user.getUsername());
            return true;
        }

        // 👤 본인 글은 수정/삭제 가능
        if (post.getUser() != null && post.getUser().getId().equals(user.getId())) {
            System.out.println("👤 본인 글 수정/삭제 허용: " + user.getUsername());
            return true;
        }

        System.out.println("❌ 수정/삭제 권한 없음: " + user.getUsername());
        return false;
    }
}