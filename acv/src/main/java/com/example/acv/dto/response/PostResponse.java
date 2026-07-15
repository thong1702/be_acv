package com.example.acv.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class PostResponse {

    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private String thumbnailUrl;
    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    private String status;
    private Integer viewCount;
    private Long createdById;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Frontend compatibility fields
    private String thumbnail;
    private CategoryDto category;
    private UserDto user;

    public PostResponse(Long id, String title, String slug, String summary, String content, String thumbnailUrl,
                        Long categoryId, String categoryName, String categorySlug, Integer status, Integer viewCount,
                        Long createdById, String createdByUsername, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.summary = summary;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categorySlug = categorySlug;
        this.status = (status != null && status == 1) ? "PUBLISHED" : "DRAFT";
        this.viewCount = viewCount;
        this.createdById = createdById;
        this.createdByUsername = createdByUsername;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;

        // Populate compatibility fields
        this.thumbnail = thumbnailUrl;
        if (categoryId != null) {
            this.category = new CategoryDto(categoryId, categoryName, categorySlug);
        }
        if (createdById != null) {
            this.user = new UserDto(createdById, createdByUsername);
        }
    }

    @Getter
    public static class CategoryDto {
        private Long id;
        private String name;
        private String slug;

        public CategoryDto(Long id, String name, String slug) {
            this.id = id;
            this.name = name;
            this.slug = slug;
        }
    }

    @Getter
    public static class UserDto {
        private Long id;
        private String username;

        public UserDto(Long id, String username) {
            this.id = id;
            this.username = username;
        }
    }
}
