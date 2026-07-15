package com.example.acv.repository;

import com.example.acv.entity.Category;
import com.example.acv.entity.CategoryType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCase(String slug);

    Page<Category> findAllByType(CategoryType type, Pageable pageable);
}
