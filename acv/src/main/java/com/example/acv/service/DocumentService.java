package com.example.acv.service;

import com.example.acv.dto.request.DocumentRequest;
import com.example.acv.dto.response.DocumentResponse;
import com.example.acv.dto.response.PageResponse;
import com.example.acv.entity.Document;
import com.example.acv.entity.User;
import com.example.acv.exception.ResourceNotFoundException;
import com.example.acv.repository.DocumentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<DocumentResponse> findAll() {
        return documentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<DocumentResponse> searchDocuments(int page, int size, String search) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Document> docPage = documentRepository.searchDocuments(
                StringUtils.hasText(search) ? search : null,
                pageable
        );

        List<DocumentResponse> content = docPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                docPage.getTotalElements(),
                docPage.getTotalPages(),
                docPage.getSize(),
                docPage.getNumber()
        );
    }

    @Transactional(readOnly = true)
    public DocumentResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    public DocumentResponse create(DocumentRequest request) {
        Document document = Document.builder()
                .documentNumber(request.getDocumentNumber())
                .title(request.getTitle())
                .fileUrl(request.getFileUrl())
                .fileType(request.getFileType())
                .fileSize(request.getFileSize())
                .description(request.getDescription())
                .issuedDate(request.getIssuedDate())
                .status(request.getStatus() == null ? 1 : request.getStatus())
                .uploadedBy(resolveUser(request.getUploadedById()))
                .build();
        return toResponse(documentRepository.save(document));
    }

    public DocumentResponse create(
            org.springframework.web.multipart.MultipartFile file,
            String docNumber,
            String publishDate,
            String title,
            String description) {

        try {
            // Save file
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String savedFilename = java.util.UUID.randomUUID().toString() + fileExtension;

            // Create uploads directory if not exists
            java.io.File uploadDir = new java.io.File("uploads");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            java.nio.file.Path filePath = java.nio.file.Paths.get("uploads", savedFilename);
            java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            String cleanExtension = "";
            if (fileExtension.startsWith(".")) {
                cleanExtension = fileExtension.substring(1).toUpperCase();
            } else if (file.getContentType() != null) {
                String contentType = file.getContentType();
                if (contentType.contains("/")) {
                    cleanExtension = contentType.substring(contentType.lastIndexOf("/") + 1).toUpperCase();
                }
            }
            if (cleanExtension.length() > 10) {
                cleanExtension = cleanExtension.substring(0, 10);
            }

            Document document = Document.builder()
                    .documentNumber(docNumber)
                    .title(title)
                    .fileUrl(savedFilename) // Save the filename or relative path
                    .fileType(cleanExtension)
                    .fileSize(file.getSize())
                    .description(description)
                    .issuedDate(java.time.LocalDate.parse(publishDate))
                    .status(1)
                    .build();

            return toResponse(documentRepository.save(document));
        } catch (java.io.IOException e) {
            throw new RuntimeException("Could not store file", e);
        }
    }

    public DocumentResponse update(Long id, DocumentRequest request) {
        Document document = getEntity(id);

        if (request.getDocumentNumber() != null) {
            document.setDocumentNumber(request.getDocumentNumber());
        }
        if (StringUtils.hasText(request.getTitle())) {
            document.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getFileUrl())) {
            document.setFileUrl(request.getFileUrl());
        }
        if (request.getFileType() != null) {
            document.setFileType(request.getFileType());
        }
        if (request.getFileSize() != null) {
            document.setFileSize(request.getFileSize());
        }
        if (request.getDescription() != null) {
            document.setDescription(request.getDescription());
        }
        if (request.getIssuedDate() != null) {
            document.setIssuedDate(request.getIssuedDate());
        }
        if (request.getStatus() != null) {
            document.setStatus(request.getStatus());
        }
        if (request.getUploadedById() != null) {
            document.setUploadedBy(resolveUser(request.getUploadedById()));
        }

        return toResponse(documentRepository.save(document));
    }

    public DocumentResponse updateWithFile(
            Long id,
            org.springframework.web.multipart.MultipartFile file,
            String docNumber,
            String publishDate,
            String title,
            String description) {

        Document document = getEntity(id);

        if (file != null && !file.isEmpty()) {
            try {
                // Delete old file if exists
                if (document.getFileUrl() != null) {
                    java.io.File oldFile = new java.io.File("uploads", document.getFileUrl());
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }

                // Save new file
                String originalFilename = file.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String savedFilename = java.util.UUID.randomUUID().toString() + fileExtension;

                java.io.File uploadDir = new java.io.File("uploads");
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                java.nio.file.Path filePath = java.nio.file.Paths.get("uploads", savedFilename);
                java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                String cleanExtension = "";
                if (fileExtension.startsWith(".")) {
                    cleanExtension = fileExtension.substring(1).toUpperCase();
                } else if (file.getContentType() != null) {
                    String contentType = file.getContentType();
                    if (contentType.contains("/")) {
                        cleanExtension = contentType.substring(contentType.lastIndexOf("/") + 1).toUpperCase();
                    }
                }
                if (cleanExtension.length() > 10) {
                    cleanExtension = cleanExtension.substring(0, 10);
                }

                document.setFileUrl(savedFilename);
                document.setFileType(cleanExtension);
                document.setFileSize(file.getSize());
            } catch (java.io.IOException e) {
                throw new RuntimeException("Could not store file", e);
            }
        }

        if (docNumber != null) {
            document.setDocumentNumber(docNumber);
        }
        if (title != null) {
            document.setTitle(title);
        }
        if (description != null) {
            document.setDescription(description);
        }
        if (publishDate != null) {
            document.setIssuedDate(java.time.LocalDate.parse(publishDate));
        }

        return toResponse(documentRepository.save(document));
    }

    public void delete(Long id) {
        documentRepository.delete(getEntity(id));
    }

    private Document getEntity(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id " + id));
    }

    private User resolveUser(Long userId) {
        if (userId == null) {
            return null;
        }
        return userService.findEntityById(userId);
    }

    private DocumentResponse toResponse(Document document) {
        User uploadedBy = document.getUploadedBy();
        return new DocumentResponse(
                document.getId(),
                document.getDocumentNumber(),
                document.getTitle(),
                document.getFileUrl(),
                document.getFileType(),
                document.getFileSize(),
                document.getDescription(),
                document.getIssuedDate(),
                document.getStatus(),
                uploadedBy == null ? null : uploadedBy.getId(),
                uploadedBy == null ? null : uploadedBy.getUsername(),
                document.getCreatedAt()
        );
    }
}
