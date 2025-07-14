package com.example.board.service;

import com.example.board.dto.AuthDto;
import com.example.board.dto.UserDto;
import com.example.board.entity.User;
import com.example.board.repository.UserRepository;
import com.example.board.security.JwtTokenProvider;
import org.slf4j.Logger;
import com.example.board.entity.RefreshToken;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public AuthService(UserRepository userRepository,
                       UserService userService,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public UserDto register(AuthDto.RegisterRequest request) {
        try {
            logger.info("회원가입 시도: {}", request.getUsername());
            return userService.registerUser(
                    request.getUsername(),
                    request.getPassword(),
                    request.getEmail(),
                    request.getName(),
                    request.getNickname()  // 닉네임 추가
            );
        } catch (Exception e) {
            logger.error("회원가입 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public AuthDto.LoginResponse login(AuthDto.LoginRequest request) {
        try {
            logger.info("로그인 시도: {}", request.getUsername());

            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("잘못된 사용자 이름 또는 비밀번호입니다."));

            logger.info("사용자 발견: {}", user.getUsername());

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                logger.info("비밀번호 불일치: {}", user.getUsername());
                throw new BadCredentialsException("잘못된 사용자 이름 또는 비밀번호입니다.");
            }

            if (!user.isEnabled()) {
                logger.info("비활성화된 계정: {}", user.getUsername());
                throw new BadCredentialsException("비활성화된 계정입니다.");
            }

            if (user.isLocked()) {
                logger.info("잠긴 계정: {}", user.getUsername());
                throw new BadCredentialsException("잠긴 계정입니다. 관리자에게 문의하세요.");
            }

            // Access Token 생성
            logger.info("Access Token 생성 중: {}", user.getUsername());
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user.getUsername(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
            );
            String accessToken = tokenProvider.createToken(authentication);
            logger.info("Access Token 생성 완료: {}", user.getUsername());

            // Refresh Token 생성
            logger.info("Refresh Token 생성 중: {}", user.getUsername());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
            logger.info("Refresh Token 생성 완료: {}", user.getUsername());

            // 유저 정보를 DTO로 변환
            UserDto userDto = UserDto.fromEntity(user);

            logger.info("로그인 성공: {} (닉네임: {})", user.getUsername(), user.getNickname());
            return new AuthDto.LoginResponse(accessToken, refreshToken.getToken(), userDto);

        } catch (Exception e) {
            logger.error("로그인 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public AuthDto.TokenRefreshResponse refreshToken(String refreshTokenStr) {
        try {
            RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr)
                    .orElseThrow(() -> new RuntimeException("유효하지 않은 refresh token입니다."));

            refreshToken = refreshTokenService.verifyExpiration(refreshToken);

            // 새로운 Access Token 생성
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    refreshToken.getUser().getUsername(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(refreshToken.getUser().getRole().name()))
            );
            String newAccessToken = tokenProvider.createToken(authentication);

            return new AuthDto.TokenRefreshResponse(newAccessToken, refreshToken.getToken());
        } catch (Exception e) {
            logger.error("토큰 갱신 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void logout(String refreshTokenStr) {
        try {
            refreshTokenService.revokeToken(refreshTokenStr);
        } catch (Exception e) {
            logger.error("로그아웃 실패: {}", e.getMessage(), e);
            throw e;
        }
    }
}