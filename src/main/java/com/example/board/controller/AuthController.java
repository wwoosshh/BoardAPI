package com.example.board.controller;

import com.example.board.dto.AuthDto;
import com.example.board.dto.UserDto;
import com.example.board.entity.User;
import com.example.board.repository.UserRepository;
import com.example.board.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;


import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(AuthService authService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthDto.RegisterRequest request) {
        try {
            UserDto user = authService.register(request);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDto.LoginRequest request) {
        try {
            // 기존 코드 대신 새로운 login 메서드 호출
            AuthDto.LoginResponse response = authService.login(request);

            // 응답 형식을 명확하게 지정
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("accessToken", response.getAccessToken());
            responseBody.put("refreshToken", response.getRefreshToken());
            responseBody.put("user", response.getUser());

            System.out.println("로그인 성공 응답: " + responseBody); // 디버그 로그

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody AuthDto.TokenRefreshRequest request) {
        try {
            AuthDto.TokenRefreshResponse response = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody AuthDto.LogoutRequest request) {
        try {
            authService.logout(request.getRefreshToken());
            Map<String, String> response = new HashMap<>();
            response.put("message", "로그아웃되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 디버깅용 테스트 엔드포인트
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth API is working!");
    }
}