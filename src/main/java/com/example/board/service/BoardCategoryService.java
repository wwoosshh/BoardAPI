package com.example.board.service;

import com.example.board.dto.BoardCategoryDto;
import com.example.board.entity.BoardCategory;
import com.example.board.repository.BoardCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BoardCategoryService {

    private final BoardCategoryRepository boardCategoryRepository;

    @Autowired
    public BoardCategoryService(BoardCategoryRepository boardCategoryRepository) {
        this.boardCategoryRepository = boardCategoryRepository;
    }

    @Transactional(readOnly = true)
    public List<BoardCategoryDto> getAllCategories() {
        return boardCategoryRepository.findAll()
                .stream()
                .map(BoardCategoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BoardCategoryDto getCategoryById(Long id) {
        BoardCategory category = boardCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다: " + id));
        return BoardCategoryDto.fromEntity(category);
    }

    @Transactional
    public BoardCategoryDto createCategory(BoardCategoryDto categoryDto) {
        BoardCategory category = categoryDto.toEntity();
        BoardCategory savedCategory = boardCategoryRepository.save(category);
        return BoardCategoryDto.fromEntity(savedCategory);
    }

    @Transactional
    public void initDefaultCategories() {
        // 이미 카테고리가 있는지 확인
        if (boardCategoryRepository.count() == 0) {
            // 기본 카테고리 생성
            createDefaultCategory("게시판 1", "첫 번째 게시판입니다.");
            createDefaultCategory("게시판 2", "두 번째 게시판입니다.");
            createDefaultCategory("게시판 3", "세 번째 게시판입니다.");
        }
    }

    private void createDefaultCategory(String name, String description) {
        BoardCategory category = BoardCategory.builder()
                .name(name)
                .description(description)
                .build();
        boardCategoryRepository.save(category);
    }
}