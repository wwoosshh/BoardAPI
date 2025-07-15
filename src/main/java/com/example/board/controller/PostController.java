// src/main/java/com/example/board/controller/PostController.java (업데이트)
package com.example.board.controller;

import com.example.board.dto.PostDto;
import com.example.board.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    // 모든 게시글 목록 조회
    @GetMapping("/posts")
    public ResponseEntity<List<PostDto>> getAllPosts() {
        List<PostDto> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    // 카테고리별 게시글 목록 조회
    @GetMapping("/categories/{categoryId}/posts")
    public ResponseEntity<List<PostDto>> getPostsByCategory(@PathVariable Long categoryId) {
        try {
            List<PostDto> posts = postService.getPostsByCategory(categoryId);
            return ResponseEntity.ok(posts);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 게시글 상세 조회
    @GetMapping("/posts/{id}")
    public ResponseEntity<PostDto> getPost(@PathVariable Long id) {
        try {
            PostDto post = postService.getPost(id);
            return ResponseEntity.ok(post);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 게시글 생성 - JSON 요청
    @PostMapping("/posts")
    public ResponseEntity<PostDto> createPost(@RequestBody PostDto postDto) {
        PostDto createdPost = postService.createPost(postDto, Collections.emptyList());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    // 게시글 생성 - 멀티파트 요청 (파일 업로드 지원)
    @PostMapping(value = "/posts/with-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDto> createPostWithFiles(
            @RequestPart("post") PostDto postDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        PostDto createdPost = postService.createPost(postDto, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    // 게시글 수정 - JSON 요청
    @PutMapping("/posts/{id}")
    public ResponseEntity<PostDto> updatePost(@PathVariable Long id, @RequestBody PostDto postDto) {
        try {
            PostDto updatedPost = postService.updatePost(id, postDto, Collections.emptyList(), Collections.emptyList());
            return ResponseEntity.ok(updatedPost);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 게시글 수정 - 멀티파트 요청 (파일 업로드 지원)
    @PutMapping(value = "/posts/{id}/with-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDto> updatePostWithFiles(
            @PathVariable Long id,
            @RequestPart("post") PostDto postDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "deleteFiles", required = false) List<Long> filesToDelete) {

        try {
            PostDto updatedPost = postService.updatePost(id, postDto, files, filesToDelete);
            return ResponseEntity.ok(updatedPost);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 게시글 삭제
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        try {
            postService.deletePost(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}