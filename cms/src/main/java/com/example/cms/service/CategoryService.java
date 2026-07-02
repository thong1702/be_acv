package com.example.cms.service;

import com.example.cms.dto.request.CategoryRequest;
import com.example.cms.dto.response.CategoryResponse;
import com.example.cms.entity.Category;
import com.example.cms.entity.CategoryType;
import com.example.cms.exception.DuplicateResourceException;
import com.example.cms.exception.ResourceNotFoundException;
import com.example.cms.repository.CategoryRepository;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s_]+");

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll(CategoryType type) {
        return (type == null ? categoryRepository.findAll() : categoryRepository.findAllByTypeOrderByCreatedAtDesc(type))
                .stream()
                .map(this::toResponse)
                .toList();
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
        String baseSlug = slugify(slugSource);
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

    private String slugify(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        normalized = WHITESPACE.matcher(normalized).replaceAll("-");
        normalized = NON_LATIN.matcher(normalized).replaceAll("");
        return normalized.replaceAll("-{2,}", "-").replaceAll("^-|-$", "");
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
