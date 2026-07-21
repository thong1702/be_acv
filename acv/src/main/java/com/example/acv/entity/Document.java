package com.example.acv.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "documents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_number", length = 100)
    private String documentNumber;

    @Column(nullable = false)
    private String title;

    @Column(name = "file_url", nullable = false, length = 555)
    private String fileUrl;

    @Column(name = "file_type", length = 10)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "original_file_name", length = 255)
    private String originalFileName;

    @Lob
    private String description;

    @Column(name = "issued_date")
    private LocalDate issuedDate;

    @Column(nullable = false)
    @Builder.Default
    private Integer status = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
