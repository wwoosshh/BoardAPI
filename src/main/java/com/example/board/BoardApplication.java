package com.example.board;

import com.example.board.entity.User;
import com.example.board.entity.UserRole;
import com.example.board.repository.UserRepository;
import com.example.board.service.BoardCategoryService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class BoardApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoardApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(BoardCategoryService boardCategoryService,
                                      UserRepository userRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            // 기본 카테고리 초기화
            boardCategoryService.initDefaultCategories();
            System.out.println("✅ 기본 카테고리 초기화 완료");

            // 관리자 계정이 없으면 자동 생성
            if (!userRepository.existsByUsername("admin")) {
                User adminUser = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .email("admin@board.com")
                        .name("시스템 관리자")
                        .role(UserRole.ROLE_ADMIN)
                        .enabled(true)
                        .locked(false)
                        .warningCount(0)
                        .build();

                userRepository.save(adminUser);
                System.out.println("🔑 === 관리자 계정이 생성되었습니다 ===");
                System.out.println("    아이디: admin");
                System.out.println("    비밀번호: admin123");
                System.out.println("    권한: 최고 관리자");
                System.out.println("=======================================");
            } else {
                System.out.println("✅ 관리자 계정이 이미 존재합니다");
            }

            // 현재 사용자 현황 출력
            long totalUsers = userRepository.count();
            long adminCount = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == UserRole.ROLE_ADMIN).count();
            long moderatorCount = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == UserRole.ROLE_MODERATOR).count();
            long userCount = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == UserRole.ROLE_USER).count();

            System.out.println("📊 현재 사용자 현황:");
            System.out.println("    - 전체: " + totalUsers + "명");
            System.out.println("    - 관리자: " + adminCount + "명");
            System.out.println("    - 관리자회원: " + moderatorCount + "명");
            System.out.println("    - 일반회원: " + userCount + "명");
        };
    }
}