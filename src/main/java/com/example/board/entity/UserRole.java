package com.example.board.entity;

public enum UserRole {
    ROLE_USER,        // 일반 회원
    ROLE_MODERATOR,   // 관리자 회원 (특정 게시판 관리)
    ROLE_ADMIN,       // 관리자 (전체 관리)
    ROLE_MANAGER      // 매니저 (최고 권한)
}