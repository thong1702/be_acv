package com.example.acv.controller;

import com.example.acv.dto.response.PageResponse;
import com.example.acv.entity.ContactRequest;
import com.example.acv.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    // Endpoint public cho khách hàng submit Form tư vấn
    @PostMapping("/submit")
    public ResponseEntity<ContactRequest> submitRequest(@RequestBody ContactRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contactService.createRequest(request));
    }

    // Endpoints cho Admin CMS quản lý
    @GetMapping
    public PageResponse<ContactRequest> searchRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer status) {
        return contactService.searchRequests(page, size, search, status);
    }

    @GetMapping("/{id}")
    public ContactRequest findById(@PathVariable Long id) {
        return contactService.findById(id);
    }

    @PutMapping("/{id}/status")
    public ContactRequest updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null) {
            status = 1;
        }
        return contactService.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contactService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pending-count")
    public Map<String, Long> countPending() {
        return Map.of("pendingCount", contactService.countPending());
    }
}
