package com.example.acv.service;

import com.example.acv.dto.request.PostRequest;
import com.example.acv.dto.response.PostResponse;
import com.example.acv.dto.response.PageResponse;
import com.example.acv.entity.Category;
import com.example.acv.entity.Post;
import com.example.acv.entity.User;
import com.example.acv.exception.DuplicateResourceException;
import com.example.acv.exception.ResourceNotFoundException;
import com.example.acv.repository.PostRepository;
import com.example.acv.util.SlugUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public List<PostResponse> findAll() {
        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PostResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public PostResponse findBySlug(String slug) {
        return toResponse(postRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with slug " + slug)));
    }

    public PostResponse create(PostRequest request) {
        String slug = resolveSlug(request.getSlug(), request.getTitle(), null);
        ensureSlugAvailable(slug, null);

        Post post = Post.builder()
                .title(request.getTitle())
                .slug(slug)
                .summary(request.getSummary())
                .content(request.getContent())
                .thumbnailUrl(request.getThumbnailUrl())
                .status(request.getStatus() == null ? 1 : ("PUBLISHED".equalsIgnoreCase(request.getStatus()) ? 1 : 0))
                .viewCount(0)
                .category(resolveCategory(request.getCategoryId()))
                .createdBy(resolveUser(request.getCreatedById()))
                .build();
        return toResponse(postRepository.save(post));
    }

    public PostResponse update(Long id, PostRequest request) {
        Post post = getEntity(id);

        if (StringUtils.hasText(request.getTitle())) {
            post.setTitle(request.getTitle());
        }
        if (request.getSummary() != null) {
            post.setSummary(request.getSummary());
        }
        if (StringUtils.hasText(request.getContent())) {
            post.setContent(request.getContent());
        }
        if (request.getThumbnailUrl() != null) {
            String oldThumbnail = post.getThumbnailUrl();
            if (oldThumbnail != null && !oldThumbnail.equals(request.getThumbnailUrl())) {
                cloudinaryService.deleteFileByUrl(oldThumbnail);
            }
            post.setThumbnailUrl(request.getThumbnailUrl());
        }
        if (request.getCategoryId() != null) {
            post.setCategory(resolveCategory(request.getCategoryId()));
        }
        if (request.getCreatedById() != null) {
            post.setCreatedBy(resolveUser(request.getCreatedById()));
        }
        if (request.getStatus() != null) {
            post.setStatus("PUBLISHED".equalsIgnoreCase(request.getStatus()) ? 1 : 0);
        }

        String slug = resolveSlug(request.getSlug(), post.getTitle(), post.getSlug());
        ensureSlugAvailable(slug, id);
        post.setSlug(slug);

        return toResponse(postRepository.save(post));
    }

    @Transactional(readOnly = true)
    public PageResponse<PostResponse> searchPosts(int page, int size, String search, Long categoryId, String status) {
        Integer statusValue = null;
        if (StringUtils.hasText(status)) {
            statusValue = "PUBLISHED".equalsIgnoreCase(status) ? 1 : 0;
        }

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Post> postsPage = postRepository.searchPosts(
                StringUtils.hasText(search) ? search : null,
                categoryId,
                statusValue,
                pageable
        );

        return PageResponse.of(postsPage, this::toResponse);
    }

    public void incrementViews(Long id) {
        Post post = getEntity(id);
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
    }

    public void delete(Long id) {
        Post post = getEntity(id);
        if (post.getThumbnailUrl() != null) {
            cloudinaryService.deleteFileByUrl(post.getThumbnailUrl());
        }
        postRepository.delete(post);
    }

    private Post getEntity(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id " + id));
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryService.getEntity(categoryId);
    }

    private User resolveUser(Long userId) {
        return userId == null ? null : userService.findEntityById(userId);
    }

    private void ensureSlugAvailable(String slug, Long currentId) {
        postRepository.findBySlugIgnoreCase(slug)
                .filter(post -> currentId == null || !post.getId().equals(currentId))
                .ifPresent(post -> {
                    throw new DuplicateResourceException("Post slug already exists");
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
        while (postRepository.existsBySlugIgnoreCase(slug)) {
            slug = baseSlug + "-" + index++;
        }
        return slug;
    }

    private PostResponse toResponse(Post post) {
        Category category = post.getCategory();
        User createdBy = post.getCreatedBy();
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getSummary(),
                post.getContent(),
                post.getThumbnailUrl(),
                category == null ? null : category.getId(),
                category == null ? null : category.getName(),
                category == null ? null : category.getSlug(),
                post.getStatus(),
                post.getViewCount(),
                createdBy == null ? null : createdBy.getId(),
                createdBy == null ? null : createdBy.getUsername(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
