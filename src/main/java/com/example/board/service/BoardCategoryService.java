package com.example.board.service;

import com.example.board.dto.BoardCategoryDto;
import com.example.board.entity.BoardCategory;
import com.example.board.repository.BoardCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
        // 이름 중복 체크
        if (boardCategoryRepository.findByName(categoryDto.getName()).isPresent()) {
            throw new RuntimeException("이미 존재하는 카테고리 이름입니다: " + categoryDto.getName());
        }

        BoardCategory category = BoardCategory.builder()
                .name(categoryDto.getName())
                .description(categoryDto.getDescription())
                .build();

        BoardCategory savedCategory = boardCategoryRepository.save(category);
        System.out.println("📋 새 카테고리 생성: " + savedCategory.getName());

        return BoardCategoryDto.fromEntity(savedCategory);
    }

    @Transactional
    public BoardCategoryDto updateCategory(Long id, BoardCategoryDto categoryDto) {
        BoardCategory category = boardCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다: " + id));

        // 이름 중복 체크 (자기 자신 제외)
        Optional<BoardCategory> existingCategory = boardCategoryRepository.findByName(categoryDto.getName());
        if (existingCategory.isPresent() && !existingCategory.get().getId().equals(id)) {
            throw new RuntimeException("이미 존재하는 카테고리 이름입니다: " + categoryDto.getName());
        }

        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());

        BoardCategory updatedCategory = boardCategoryRepository.save(category);
        System.out.println("📋 카테고리 수정 완료: " + updatedCategory.getName());

        return BoardCategoryDto.fromEntity(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        BoardCategory category = boardCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다: " + id));

        String categoryName = category.getName();
        boardCategoryRepository.delete(category);
        System.out.println("🗑️ 카테고리 삭제 완료: " + categoryName);
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