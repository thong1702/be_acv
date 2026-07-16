package com.example.acv.service;

import com.example.acv.dto.request.CategoryRequest;
import com.example.acv.dto.response.CategoryResponse;
import com.example.acv.dto.response.PageResponse;
import com.example.acv.entity.Category;
import com.example.acv.entity.CategoryType;
import com.example.acv.exception.DuplicateResourceException;
import com.example.acv.exception.ResourceNotFoundException;
import com.example.acv.repository.CategoryRepository;
import com.example.acv.util.SlugUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> findAll(int page, int size, CategoryType type) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<Category> categoriesPage;
        if (type == null) {
            categoriesPage = categoryRepository.findAll(pageable);
        } else {
            categoriesPage = categoryRepository.findAllByType(type, pageable);
        }
        return PageResponse.of(categoriesPage, this::toResponse);
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    public CategoryResponse create(CategoryRequest request) {
        String slug = resolveSlug(request.getSlug(), request.getName(), null);
        ensureSlugAvailable(slug, null);

        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .type(request.getType())
                .description(request.getDescription())
                .build();
        return toResponse(categoryRepository.save(category));
    }

    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getEntity(id);

        if (StringUtils.hasText(request.getName())) {
            category.setName(request.getName());
        }
        category.setType(request.getType());
        category.setDescription(request.getDescription());

        String slug = resolveSlug(request.getSlug(), category.getName(), category.getSlug());
        ensureSlugAvailable(slug, id);
        category.setSlug(slug);

        return toResponse(categoryRepository.save(category));
    }

    public void delete(Long id) {
        categoryRepository.delete(getEntity(id));
    }

    @Transactional(readOnly = true)
    public Category getEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));
    }

    private void ensureSlugAvailable(String slug, Long currentId) {
        categoryRepository.findBySlugIgnoreCase(slug)
                .filter(category -> currentId == null || !category.getId().equals(currentId))
                .ifPresent(category -> {
                    throw new DuplicateResourceException("Category slug already exists");
                });
    }

    private String resolveSlug(String customSlug, String source, String currentSlug) {
        String slugSource = StringUtils.hasText(customSlug) ? customSlug : source;
        String baseSlug = SlugUtil.slugify(slugSource);
        if (currentSlug != null && currentSlug.equalsIgnoreCase(baseSlug)) {
            return currentSlug;
        }
        String slug = baseSlug;
        int index = 2;
        while (categoryRepository.existsBySlugIgnoreCase(slug)) {
            slug = baseSlug + "-" + index++;
        }
        return slug;
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getType(),
                category.getDescription(),
                category.getCreatedAt()
        );
    }
}
