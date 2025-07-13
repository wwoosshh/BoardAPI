package com.example.board.controller;

import com.example.board.dto.BoardCategoryDto;
import com.example.board.service.BoardCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class BoardCategoryController {

    private final BoardCategoryService boardCategoryService;

    @Autowired
    public BoardCategoryController(BoardCategoryService boardCategoryService) {
        this.boardCategoryService = boardCategoryService;
    }

    @GetMapping
    public ResponseEntity<List<BoardCategoryDto>> getAllCategories() {
        List<BoardCategoryDto> categories = boardCategoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardCategoryDto> getCategory(@PathVariable Long id) {
        try {
            BoardCategoryDto category = boardCategoryService.getCategoryById(id);
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}