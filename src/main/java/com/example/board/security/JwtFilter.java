package com.example.board.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    private final JwtTokenProvider tokenProvider;

    public JwtFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        logger.debug("🔍 요청 처리: {} {}", method, path);

        // 인증 관련 요청은 필터를 건너뛰도록 설정
        if (path.startsWith("/api/auth/")) {
            logger.debug("✅ 인증 경로 요청 감지: {}, 필터 건너뜀", path);
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 확인 및 인증 처리
        String jwt = resolveToken(request);
        logger.debug("🔑 JWT 토큰 확인: {}", jwt != null ? "존재함" : "없음");

        if (jwt != null) {
            logger.debug("🔍 토큰 내용: {}...", jwt.substring(0, Math.min(jwt.length(), 20)));
        }

        if (StringUtils.hasText(jwt)) {
            boolean isValid = tokenProvider.validateToken(jwt);
            logger.debug("🔒 토큰 유효성: {}", isValid ? "유효" : "무효");

            if (isValid) {
                Authentication authentication = tokenProvider.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("✅ 유효한 JWT 토큰으로 인증 완료: {}", authentication.getName());
                logger.debug("🏷️ 사용자 권한: {}", authentication.getAuthorities());
            } else {
                logger.warn("❌ 유효하지 않은 JWT 토큰");
            }
        } else {
            logger.debug("⚠️ JWT 토큰이 없음 - 익명 요청");
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        logger.debug("📨 Authorization 헤더: {}", bearerToken != null ? "존재함" : "없음");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            logger.debug("🎯 추출된 토큰: {}...", token.substring(0, Math.min(token.length(), 20)));
            return token;
        }
        return null;
    }
}