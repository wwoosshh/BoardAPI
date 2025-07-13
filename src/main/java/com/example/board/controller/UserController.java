package com.example.board.controller;

import com.example.board.dto.UserDto;
import com.example.board.entity.UserRole;
import com.example.board.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserDto> updateUserRole(@PathVariable Long id, @RequestParam UserRole role) {
        UserDto user = userService.updateUserRole(id, role);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{userId}/categories/{categoryId}")
    public ResponseEntity<UserDto> assignCategoryToModerator(
            @PathVariable Long userId, @PathVariable Long categoryId) {
        UserDto user = userService.assignCategoryToModerator(userId, categoryId);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{userId}/categories/{categoryId}")
    public ResponseEntity<UserDto> removeCategoryFromModerator(
            @PathVariable Long userId, @PathVariable Long categoryId) {
        UserDto user = userService.removeCategoryFromModerator(userId, categoryId);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{id}/warn")
    public ResponseEntity<UserDto> warnUser(@PathVariable Long id) {
        UserDto user = userService.warnUser(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/lock")
    public ResponseEntity<UserDto> lockUser(@PathVariable Long id, @RequestParam boolean lock) {
        UserDto user = userService.lockUser(id, lock);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}