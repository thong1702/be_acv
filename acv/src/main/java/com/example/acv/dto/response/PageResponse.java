package com.example.acv.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int size;
    private int number;

    public static <S, T> PageResponse<T> of(org.springframework.data.domain.Page<S> page,
            java.util.function.Function<S, T> mapper) {
        List<T> contentList = page.getContent().stream().map(mapper).toList();
        return new PageResponse<>(
                contentList,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber());
    }
}
