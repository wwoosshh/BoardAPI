package com.example.board.repository;

import com.example.board.entity.BoardCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardCategoryRepository extends JpaRepository<BoardCategory, Long> {
    Optional<BoardCategory> findByName(String name);
}