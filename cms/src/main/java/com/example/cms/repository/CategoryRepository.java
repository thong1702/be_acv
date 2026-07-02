package com.example.cms.repository;

import com.example.cms.entity.Category;
import com.example.cms.entity.CategoryType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCase(String slug);

    List<Category> findAllByTypeOrderByCreatedAtDesc(CategoryType type);
}
