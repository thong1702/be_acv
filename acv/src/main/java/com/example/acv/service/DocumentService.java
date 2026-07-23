package com.example.acv.service;

import com.example.acv.dto.request.DocumentRequest;
import com.example.acv.dto.response.DocumentResponse;
import com.example.acv.dto.response.PageResponse;
import com.example.acv.dto.response.DocumentDownloadInfo;
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
    private final CloudinaryService cloudinaryService;

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
        return PageResponse.of(docPage, this::toResponse);
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
            java.util.Map uploadResult = cloudinaryService.uploadDocument(file);
            String fileUrl = uploadResult.get("secure_url").toString();

            String cleanExtension = "";
            if (uploadResult.get("format") != null) {
                cleanExtension = uploadResult.get("format").toString().toUpperCase();
            } else {
                String originalFilename = file.getOriginalFilename();
                if (originalFilename != null && originalFilename.contains(".")) {
                    cleanExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toUpperCase();
                }
            }
            if (cleanExtension.length() > 10) {
                cleanExtension = cleanExtension.substring(0, 10);
            }

            Document document = Document.builder()
                    .documentNumber(docNumber)
                    .title(title)
                    .fileUrl(fileUrl)
                    .fileType(cleanExtension)
                    .fileSize(file.getSize())
                    .originalFileName(file.getOriginalFilename())
                    .description(description)
                    .issuedDate(java.time.LocalDate.parse(publishDate))
                    .status(1)
                    .build();

            return toResponse(documentRepository.save(document));
        } catch (Exception e) {
            throw new com.example.acv.exception.BadRequestException("Lỗi lưu file lên Cloudinary: " + e.getMessage());
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
                if (document.getFileUrl() != null) {
                    cloudinaryService.deleteFileByUrl(document.getFileUrl());
                }
                java.util.Map uploadResult = cloudinaryService.uploadDocument(file);
                String fileUrl = uploadResult.get("secure_url").toString();

                String cleanExtension = "";
                if (uploadResult.get("format") != null) {
                    cleanExtension = uploadResult.get("format").toString().toUpperCase();
                } else {
                    String originalFilename = file.getOriginalFilename();
                    if (originalFilename != null && originalFilename.contains(".")) {
                        cleanExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toUpperCase();
                    }
                }
                if (cleanExtension.length() > 10) {
                    cleanExtension = cleanExtension.substring(0, 10);
                }

                document.setFileUrl(fileUrl);
                document.setFileType(cleanExtension);
                document.setFileSize(file.getSize());
                document.setOriginalFileName(file.getOriginalFilename());
            } catch (Exception e) {
                throw new com.example.acv.exception.BadRequestException("Lỗi lưu file lên Cloudinary: " + e.getMessage());
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

    @Transactional(readOnly = true)
    public DocumentDownloadInfo getDownloadInfo(Long id) {
        Document document = getEntity(id);
        String fileUrl = document.getFileUrl();

        // File lưu trên Cloudinary (hoặc external URL) → redirect trực tiếp
        if (fileUrl != null && fileUrl.startsWith("http")) {
            return new DocumentDownloadInfo(null, null, 0, null, fileUrl);
        }

        java.io.File file = new java.io.File("uploads", fileUrl);
        if (!file.exists()) {
            throw new ResourceNotFoundException("File not found on server");
        }

        org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(file);

        String fileType = document.getFileType();
        String mimeType = "application/octet-stream";
        String fileExtension = "";

        if (fileType != null) {
            String upper = fileType.trim().toUpperCase();
            if (upper.equals("PDF")) {
                mimeType = "application/pdf";
                fileExtension = ".pdf";
            } else if (upper.equals("DOCX") || upper.equals("DOC")) {
                mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                fileExtension = upper.equals("DOCX") ? ".docx" : ".doc";
            } else if (upper.equals("XLSX") || upper.equals("XLS")) {
                mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                fileExtension = upper.equals("XLSX") ? ".xlsx" : ".xls";
            } else if (upper.equals("PNG")) {
                mimeType = "image/png";
                fileExtension = ".png";
            } else if (upper.equals("JPG") || upper.equals("JPEG")) {
                mimeType = "image/jpeg";
                fileExtension = upper.equals("JPG") ? ".jpg" : ".jpeg";
            } else if (upper.contains("/")) {
                mimeType = fileType;
            }
        }

        String downloadFilename = document.getTitle() + fileExtension;
        return new DocumentDownloadInfo(downloadFilename, mimeType, file.length(), resource, null);
    }

    public void delete(Long id) {
        Document document = getEntity(id);
        if (document.getFileUrl() != null) {
            cloudinaryService.deleteFileByUrl(document.getFileUrl());
        }
        documentRepository.delete(document);
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
                document.getOriginalFileName(),
                document.getDescription(),
                document.getIssuedDate(),
                document.getStatus(),
                uploadedBy == null ? null : uploadedBy.getId(),
                uploadedBy == null ? null : uploadedBy.getUsername(),
                document.getCreatedAt()
        );
    }
}
