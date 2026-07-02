package com.example.cms.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DocumentResponse {

    private Long id;
    private String documentNumber;
    private String title;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private String description;
    private LocalDate issuedDate;
    private Integer status;
    private Long uploadedById;
    private String uploadedByUsername;
    private LocalDateTime createdAt;
}
