package com.example.cms.dto.response;

import com.example.cms.entity.CategoryType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private String slug;
    private CategoryType type;
    private String description;
    private LocalDateTime createdAt;
}
