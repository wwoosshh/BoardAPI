package com.example.board.dto;

import com.example.board.entity.BoardCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardCategoryDto {

    private Long id;
    private String name;
    private String description;

    public static BoardCategoryDto fromEntity(BoardCategory category) {
        return BoardCategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    public BoardCategory toEntity() {
        return BoardCategory.builder()
                .id(id)
                .name(name)
                .description(description)
                .build();
    }
}