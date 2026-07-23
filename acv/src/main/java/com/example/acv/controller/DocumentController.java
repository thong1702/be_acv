package com.example.acv.controller;

import com.example.acv.dto.request.DocumentRequest;
import com.example.acv.dto.response.DocumentResponse;
import com.example.acv.dto.response.PageResponse;
import com.example.acv.dto.response.DocumentDownloadInfo;
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
    public ResponseEntity<?> downloadFile(
            @PathVariable Long id,
            @RequestParam(value = "inline", defaultValue = "false") boolean inline) {
        DocumentDownloadInfo info = documentService.getDownloadInfo(id);

        if (info.getResource() == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Không thể tải file, vui lòng thử lại.");
        }

        String disposition = inline ? "inline" : "attachment";
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + info.getFilename() + "\"")
                .contentType(org.springframework.http.MediaType.parseMediaType(info.getMimeType()))
                .contentLength(info.getContentLength())
                .body(info.getResource());
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
