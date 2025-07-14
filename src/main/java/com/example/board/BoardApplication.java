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

            // 매니저 계정이 없으면 자동 생성
            if (!userRepository.existsByUsername("manager")) {
                User managerUser = User.builder()
                        .username("manager")
                        .password(passwordEncoder.encode("manager123"))
                        .email("manager@board.com")
                        .name("시스템 매니저")
                        .nickname("매니저")  // 닉네임 설정
                        .role(UserRole.ROLE_MANAGER)
                        .enabled(true)
                        .locked(false)
                        .warningCount(0)
                        .build();

                userRepository.save(managerUser);
                System.out.println("👑 === 매니저 계정이 생성되었습니다 ===");
                System.out.println("    아이디: manager");
                System.out.println("    비밀번호: manager123");
                System.out.println("    닉네임: 매니저");
                System.out.println("    권한: 최고 매니저");
                System.out.println("=======================================");
            } else {
                System.out.println("✅ 매니저 계정이 이미 존재합니다");
            }

            // 관리자 계정이 없으면 자동 생성
            if (!userRepository.existsByUsername("admin")) {
                User adminUser = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .email("admin@board.com")
                        .name("시스템 관리자")
                        .nickname("관리자")  // 닉네임 설정
                        .role(UserRole.ROLE_ADMIN)
                        .enabled(true)
                        .locked(false)
                        .warningCount(0)
                        .build();

                userRepository.save(adminUser);
                System.out.println("🔑 === 관리자 계정이 생성되었습니다 ===");
                System.out.println("    아이디: admin");
                System.out.println("    비밀번호: admin123");
                System.out.println("    닉네임: 관리자");
                System.out.println("    권한: 관리자");
                System.out.println("=======================================");
            } else {
                System.out.println("✅ 관리자 계정이 이미 존재합니다");
            }

            // 기존 사용자들의 닉네임 설정 (마이그레이션)
            long usersWithoutNickname = userRepository.findAll().stream()
                    .filter(user -> user.getNickname() == null || user.getNickname().trim().isEmpty())
                    .peek(user -> {
                        String nickname = user.getName() != null && !user.getName().trim().isEmpty()
                                ? user.getName() : user.getUsername();

                        // 닉네임 중복 체크 및 조정
                        String finalNickname = nickname;
                        int counter = 1;
                        while (userRepository.findByNickname(finalNickname).isPresent()) {
                            finalNickname = nickname + counter;
                            counter++;
                        }

                        user.setNickname(finalNickname);
                        userRepository.save(user);
                        System.out.println("🔄 닉네임 설정: " + user.getUsername() + " → " + finalNickname);
                    })
                    .count();

            if (usersWithoutNickname > 0) {
                System.out.println("✅ " + usersWithoutNickname + "명의 사용자 닉네임 설정 완료");
            }

            // 현재 사용자 현황 출력
            long totalUsers = userRepository.count();
            long managerCount = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == UserRole.ROLE_MANAGER).count();
            long adminCount = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == UserRole.ROLE_ADMIN).count();
            long moderatorCount = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == UserRole.ROLE_MODERATOR).count();
            long userCount = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == UserRole.ROLE_USER).count();

            System.out.println("📊 현재 사용자 현황:");
            System.out.println("    - 전체: " + totalUsers + "명");
            System.out.println("    - 매니저: " + managerCount + "명");
            System.out.println("    - 관리자: " + adminCount + "명");
            System.out.println("    - 관리자회원: " + moderatorCount + "명");
            System.out.println("    - 일반회원: " + userCount + "명");
        };
    }
}