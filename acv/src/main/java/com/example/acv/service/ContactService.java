package com.example.acv.service;

import com.example.acv.dto.response.PageResponse;
import com.example.acv.entity.ContactRequest;
import com.example.acv.exception.ResourceNotFoundException;
import com.example.acv.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class ContactService {

    private final ContactRepository contactRepository;
    private final EmailService emailService;

    public ContactRequest createRequest(ContactRequest request) {
        if (!StringUtils.hasText(request.getFullName())) {
            throw new IllegalArgumentException("Họ và tên không được để trống");
        }
        if (!StringUtils.hasText(request.getPhone())) {
            throw new IllegalArgumentException("Số điện thoại không được để trống");
        }

        request.setStatus(0); // Mặc định: 0 - Chưa phản hồi
        ContactRequest saved = contactRepository.save(request);

        // Kích hoạt gửi email thông báo tự động (bất đồng bộ)
        try {
            emailService.sendContactNotificationEmail(saved);
        } catch (Exception e) {
            // Log lỗi nhưng vẫn trả về kết quả thành công cho client
            System.err.println("Lỗi khi gửi email thông báo: " + e.getMessage());
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public PageResponse<ContactRequest> searchRequests(int page, int size, String search, Integer status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ContactRequest> requestPage = contactRepository.searchRequests(
                StringUtils.hasText(search) ? search.trim() : null,
                status,
                pageable
        );
        return PageResponse.of(requestPage, req -> req);
    }

    @Transactional(readOnly = true)
    public ContactRequest findById(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu liên hệ với id: " + id));
    }

    public ContactRequest updateStatus(Long id, Integer status) {
        ContactRequest request = findById(id);
        request.setStatus(status);
        return contactRepository.save(request);
    }

    public void delete(Long id) {
        ContactRequest request = findById(id);
        contactRepository.delete(request);
    }

    @Transactional(readOnly = true)
    public long countPending() {
        return contactRepository.countByStatus(0);
    }
}
