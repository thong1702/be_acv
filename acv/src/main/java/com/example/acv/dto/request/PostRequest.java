package com.example.acv.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    @Size(max = 255)
    private String slug;

    private String summary;

    @NotBlank
    private String content;

    @Size(max = 555)
    private String thumbnailUrl;

    @Positive
    private Long categoryId;

    private String status;

    @Positive
    private Long createdById;
}
