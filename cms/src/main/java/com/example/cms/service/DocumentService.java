package com.example.cms.service;

import com.example.cms.dto.request.DocumentRequest;
import com.example.cms.dto.response.DocumentResponse;
import com.example.cms.entity.Document;
import com.example.cms.entity.User;
import com.example.cms.exception.ResourceNotFoundException;
import com.example.cms.repository.DocumentRepository;
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
