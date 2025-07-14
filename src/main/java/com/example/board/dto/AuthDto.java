package com.example.board.dto;

import lombok.Data;

public class AuthDto {

    @Data
    public static class RegisterRequest {
        private String username;
        private String password;
        private String email;
        private String name;
        private String nickname;  // 닉네임 필드 추가
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    public static class LoginResponse {
        private String accessToken;
        private String refreshToken;
        private UserDto user;

        public LoginResponse(String accessToken, String refreshToken, UserDto user) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.user = user;
        }
    }

    @Data
    public static class TokenRefreshRequest {
        private String refreshToken;
    }

    @Data
    public static class TokenRefreshResponse {
        private String accessToken;
        private String refreshToken;

        public TokenRefreshResponse(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }

    @Data
    public static class LogoutRequest {
        private String refreshToken;
    }
}