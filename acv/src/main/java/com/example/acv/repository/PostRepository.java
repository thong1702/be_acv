package com.example.acv.repository;

import com.example.acv.entity.Post;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCase(String slug);

    List<Post> findAllByOrderByCreatedAtDesc();

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Post p WHERE " +
           "(:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR p.summary LIKE CONCAT('%', :search, '%')) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:status IS NULL OR p.status = :status) " +
           "ORDER BY p.createdAt DESC")
    org.springframework.data.domain.Page<Post> searchPosts(
            @org.springframework.data.repository.query.Param("search") String search,
            @org.springframework.data.repository.query.Param("categoryId") Long categoryId,
            @org.springframework.data.repository.query.Param("status") Integer status,
            org.springframework.data.domain.Pageable pageable);
}
