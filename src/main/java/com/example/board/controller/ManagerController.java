package com.example.board.controller;

import com.example.board.dto.BoardCategoryDto;
import com.example.board.dto.UserDto;
import com.example.board.entity.UserRole;
import com.example.board.service.BoardCategoryService;
import com.example.board.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('MANAGER')")
public class ManagerController {

    private final UserService userService;
    private final BoardCategoryService boardCategoryService;

    @Autowired
    public ManagerController(UserService userService, BoardCategoryService boardCategoryService) {
        this.userService = userService;
        this.boardCategoryService = boardCategoryService;
    }

    // ================================
    // 사용자 관리 (매니저 전용)
    // ================================

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserDto> updateUserRole(@PathVariable Long id, @RequestParam UserRole role) {
        // 매니저만 다른 사용자의 권한을 변경할 수 있음 (MANAGER 권한 제외)
        if (role == UserRole.ROLE_MANAGER) {
            return ResponseEntity.badRequest().build();
        }
        UserDto user = userService.updateUserRole(id, role);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/users/{id}/suspend")
    public ResponseEntity<UserDto> suspendUser(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean suspend) {
        UserDto user = userService.lockUser(id, suspend);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/users/{id}/warn")
    public ResponseEntity<UserDto> warnUser(@PathVariable Long id) {
        UserDto user = userService.warnUser(id);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ================================
    // 게시판 관리 (매니저 전용)
    // ================================

    @GetMapping("/categories")
    public ResponseEntity<List<BoardCategoryDto>> getAllCategories() {
        List<BoardCategoryDto> categories = boardCategoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/categories")
    public ResponseEntity<BoardCategoryDto> createCategory(@RequestBody BoardCategoryDto categoryDto) {
        BoardCategoryDto createdCategory = boardCategoryService.createCategory(categoryDto);
        return ResponseEntity.ok(createdCategory);
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<BoardCategoryDto> updateCategory(@PathVariable Long id, @RequestBody BoardCategoryDto categoryDto) {
        BoardCategoryDto updatedCategory = boardCategoryService.updateCategory(id, categoryDto);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        boardCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // ================================
    // 관리자회원 게시판 할당 (매니저 전용)
    // ================================

    @PostMapping("/users/{userId}/categories/{categoryId}")
    public ResponseEntity<UserDto> assignCategoryToModerator(@PathVariable Long userId, @PathVariable Long categoryId) {
        UserDto user = userService.assignCategoryToModerator(userId, categoryId);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/users/{userId}/categories/{categoryId}")
    public ResponseEntity<UserDto> removeCategoryFromModerator(@PathVariable Long userId, @PathVariable Long categoryId) {
        UserDto user = userService.removeCategoryFromModerator(userId, categoryId);
        return ResponseEntity.ok(user);
    }

    // ================================
    // 매니저 전용 대시보드 정보
    // ================================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = userService.getManagerDashboard();
        return ResponseEntity.ok(dashboard);
    }
}