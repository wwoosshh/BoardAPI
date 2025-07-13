package com.example.board.repository;

import com.example.board.entity.RefreshToken;
import com.example.board.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByTokenAndIsRevokedFalse(String token);

    // 사용자의 모든 토큰 무효화
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.user = :user")
    void revokeAllTokensByUser(User user);

    // 만료된 토큰 삭제
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now OR rt.isRevoked = true")
    void deleteExpiredAndRevokedTokens(LocalDateTime now);
}