package com.example.acv.controller;

import com.example.acv.dto.request.DocumentRequest;
import com.example.acv.dto.response.DocumentResponse;
import com.example.acv.dto.response.PageResponse;
import com.example.acv.service.DocumentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    public PageResponse<DocumentResponse> getDocuments(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "search", required = false) String search) {
        return documentService.searchDocuments(page, size, search);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(
            @PathVariable Long id,
            @RequestParam(value = "inline", defaultValue = "false") boolean inline) {
        DocumentResponse doc = documentService.findById(id);
        java.io.File file = new java.io.File("uploads", doc.getFileUrl()); // fileUrl stores the UUID filename
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(file);

        // Map short extension names to valid MIME types
        String fileType = doc.getFileType();
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

        String disposition = inline ? "inline" : "attachment";

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + doc.getTitle() + fileExtension + "\"")
                .contentType(org.springframework.http.MediaType.parseMediaType(mimeType))
                .contentLength(file.length())
                .body(resource);
    }

    @GetMapping("/{id}")
    public DocumentResponse findById(@PathVariable Long id) {
        return documentService.findById(id);
    }

    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> create(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam("docNumber") String docNumber,
            @RequestParam("publishDate") String publishDate,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.create(file, docNumber, publishDate, title, description));
    }

    @PutMapping("/{id}")
    public DocumentResponse update(@PathVariable Long id, @Valid @RequestBody DocumentRequest request) {
        return documentService.update(id, request);
    }

    @PutMapping(value = "/{id}/multipart", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> updateWithFile(
            @PathVariable Long id,
            @RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file,
            @RequestParam("docNumber") String docNumber,
            @RequestParam("publishDate") String publishDate,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description) {

        return ResponseEntity.ok(documentService.updateWithFile(id, file, docNumber, publishDate, title, description));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
