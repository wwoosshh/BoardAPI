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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        return registerUser(username, password, email, name, null);
    }

    @Transactional
    public UserDto registerUser(String username, String password, String email, String name, String nickname) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("이미 사용 중인 사용자 이름입니다.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        // 닉네임 설정 (제공되지 않으면 이름 또는 사용자명 사용)
        String finalNickname = nickname;
        if (finalNickname == null || finalNickname.trim().isEmpty()) {
            finalNickname = name != null && !name.trim().isEmpty() ? name : username;
        }

        // 닉네임 중복 체크 및 자동 조정
        finalNickname = ensureUniqueNickname(finalNickname);

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .name(name)
                .nickname(finalNickname)
                .role(UserRole.ROLE_USER)
                .enabled(true)
                .locked(false)
                .warningCount(0)
                .build();

        User savedUser = userRepository.save(user);

        System.out.println("👤 새 사용자 등록: " + savedUser.getUsername() +
                " (닉네임: " + savedUser.getNickname() + ")");

        return UserDto.fromEntity(savedUser);
    }

    // 닉네임 중복 체크 및 고유한 닉네임 생성
    private String ensureUniqueNickname(String baseNickname) {
        String nickname = baseNickname;
        int counter = 1;

        while (userRepository.findByNickname(nickname).isPresent()) {
            nickname = baseNickname + counter;
            counter++;
        }

        return nickname;
    }

    @Transactional
    public UserDto updateUserRole(Long userId, UserRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        // 매니저 권한은 직접 변경할 수 없음 (보안상 이유)
        if (role == UserRole.ROLE_MANAGER) {
            throw new RuntimeException("매니저 권한은 시스템에서만 부여할 수 있습니다.");
        }

        // 매니저 계정은 권한 변경할 수 없음
        if (user.getRole() == UserRole.ROLE_MANAGER) {
            throw new RuntimeException("매니저 계정의 권한은 변경할 수 없습니다.");
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

        // 매니저는 삭제할 수 없음
        if (user.getRole() == UserRole.ROLE_MANAGER) {
            throw new RuntimeException("매니저 계정은 삭제할 수 없습니다.");
        }

        String username = user.getUsername();
        userRepository.delete(user);

        System.out.println("🗑️ 사용자 삭제: " + username);
    }

    // 매니저 대시보드 정보
    @Transactional(readOnly = true)
    public Map<String, Object> getManagerDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        // 사용자 통계
        long totalUsers = userRepository.count();
        long managerCount = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.ROLE_MANAGER).count();
        long adminCount = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.ROLE_ADMIN).count();
        long moderatorCount = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.ROLE_MODERATOR).count();
        long userCount = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.ROLE_USER).count();

        Map<String, Long> userStats = new HashMap<>();
        userStats.put("total", totalUsers);
        userStats.put("manager", managerCount);
        userStats.put("admin", adminCount);
        userStats.put("moderator", moderatorCount);
        userStats.put("user", userCount);

        dashboard.put("userStats", userStats);

        // 최근 가입한 사용자들
        List<UserDto> recentUsers = userRepository.findAll().stream()
                .sorted((u1, u2) -> u2.getCreatedDate().compareTo(u1.getCreatedDate()))
                .limit(5)
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());

        dashboard.put("recentUsers", recentUsers);

        return dashboard;
    }

    // 사용자 검색 메서드
    @Transactional(readOnly = true)
    public List<UserDto> searchUsers(String keyword) {
        return userRepository.findAll().stream()
                .filter(user ->
                        user.getUsername().toLowerCase().contains(keyword.toLowerCase()) ||
                                user.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                                user.getEmail().toLowerCase().contains(keyword.toLowerCase()) ||
                                user.getNickname().toLowerCase().contains(keyword.toLowerCase())
                )
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 역할별 사용자 조회
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByRole(UserRole role) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == role)
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }
}