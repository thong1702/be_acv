package com.example.acv.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class DocumentResponse {

    private Long id;
    private String documentNumber;
    private String title;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private String originalFileName;
    private String description;
    private LocalDate issuedDate;
    private Integer status;
    private Long uploadedById;
    private String uploadedByUsername;
    private LocalDateTime createdAt;

    // Frontend compatibility fields
    private String docNumber;
    private String filePath;
    private String publishDate;

    public DocumentResponse(Long id, String documentNumber, String title, String fileUrl, String fileType,
                            Long fileSize, String originalFileName, String description, LocalDate issuedDate, Integer status,
                            Long uploadedById, String uploadedByUsername, LocalDateTime createdAt) {
        this.id = id;
        this.documentNumber = documentNumber;
        this.title = title;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.originalFileName = originalFileName;
        this.description = description;
        this.issuedDate = issuedDate;
        this.status = status;
        this.uploadedById = uploadedById;
        this.uploadedByUsername = uploadedByUsername;
        this.createdAt = createdAt;

        // Populate frontend compatibility fields
        this.docNumber = documentNumber;
        this.filePath = fileUrl;
        this.publishDate = issuedDate != null ? issuedDate.toString() : (createdAt != null ? createdAt.toLocalDate().toString() : "");
    }
}
