package com.example.acv.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationNodeRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 255)
    private String position;

    private String description;

    @Size(max = 500)
    private String avatarUrl;

    @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String phone;

    private Integer orderIndex;

    private Integer parentId;
}
