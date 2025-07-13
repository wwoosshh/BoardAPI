package com.example.board.service;

import com.example.board.dto.UserDto;
import com.example.board.entity.BoardCategory;
import com.example.board.entity.User;
import com.example.board.entity.UserRole;
import com.example.board.repository.BoardCategoryRepository;
import com.example.board.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BoardCategoryRepository boardCategoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       BoardCategoryRepository boardCategoryRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.boardCategoryRepository = boardCategoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + id));
        return UserDto.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + username));
        return UserDto.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto registerUser(String username, String password, String email, String name) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("이미 사용 중인 사용자 이름입니다.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .name(name)
                .role(UserRole.ROLE_USER)
                .enabled(true)  // 👈 명시적으로 true 설정
                .locked(false)  // 👈 명시적으로 false 설정
                .warningCount(0) // 👈 명시적으로 0 설정
                .build();

        User savedUser = userRepository.save(user);
        return UserDto.fromEntity(savedUser);
    }

    @Transactional
    public UserDto updateUserRole(Long userId, UserRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        // 관리자 권한은 직접 변경할 수 없음 (보안상 이유)
        if (role == UserRole.ROLE_ADMIN) {
            throw new RuntimeException("관리자 권한은 시스템에서만 부여할 수 있습니다.");
        }

        UserRole oldRole = user.getRole();
        user.setRole(role);

        // 일반회원으로 변경 시 관리 게시판 권한 모두 해제
        if (role == UserRole.ROLE_USER) {
            user.getManagedCategories().clear();
        }

        User updatedUser = userRepository.save(user);

        System.out.println("🔄 권한 변경: " + user.getUsername() + " (" + oldRole + " → " + role + ")");

        return UserDto.fromEntity(updatedUser);
    }

    // 관리자회원에게 특정 게시판 관리 권한 부여
    @Transactional
    public UserDto assignCategoryToModerator(Long userId, Long categoryId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        if (user.getRole() != UserRole.ROLE_MODERATOR) {
            throw new RuntimeException("관리자회원만 게시판 관리 권한을 부여받을 수 있습니다.");
        }

        BoardCategory category = boardCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("게시판을 찾을 수 없습니다: " + categoryId));

        // 이미 할당된 게시판인지 확인
        if (user.getManagedCategories().contains(category)) {
            throw new RuntimeException("이미 해당 게시판의 관리 권한을 가지고 있습니다.");
        }

        user.getManagedCategories().add(category);
        User updatedUser = userRepository.save(user);

        System.out.println("📋 게시판 할당: " + user.getUsername() + " → " + category.getName());

        return UserDto.fromEntity(updatedUser);
    }

    // 관리자회원에게서 특정 게시판 관리 권한 해제
    @Transactional
    public UserDto removeCategoryFromModerator(Long userId, Long categoryId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        BoardCategory category = boardCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("게시판을 찾을 수 없습니다: " + categoryId));

        if (!user.getManagedCategories().contains(category)) {
            throw new RuntimeException("해당 게시판의 관리 권한을 가지고 있지 않습니다.");
        }

        user.getManagedCategories().remove(category);
        User updatedUser = userRepository.save(user);

        System.out.println("📋 게시판 해제: " + user.getUsername() + " ← " + category.getName());

        return UserDto.fromEntity(updatedUser);
    }

    // 사용자 경고 부여
    @Transactional
    public UserDto warnUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        user.setWarningCount(user.getWarningCount() + 1);
        User updatedUser = userRepository.save(user);

        System.out.println("⚠️ 경고 부여: " + user.getUsername() + " (총 " + user.getWarningCount() + "회)");

        return UserDto.fromEntity(updatedUser);
    }

    // 사용자 계정 잠금/해제
    @Transactional
    public UserDto lockUser(Long userId, boolean lock) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        user.setLocked(lock);
        User updatedUser = userRepository.save(user);

        System.out.println(lock ? "🔒 계정 잠금: " : "🔓 계정 해제: " + user.getUsername());

        return UserDto.fromEntity(updatedUser);
    }

    // 사용자 삭제 (신중하게!)
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        // 관리자는 삭제할 수 없음
        if (user.getRole() == UserRole.ROLE_ADMIN) {
            throw new RuntimeException("관리자 계정은 삭제할 수 없습니다.");
        }

        String username = user.getUsername();
        userRepository.delete(user);

        System.out.println("🗑️ 사용자 삭제: " + username);
    }
}