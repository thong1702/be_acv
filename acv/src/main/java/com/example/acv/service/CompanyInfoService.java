package com.example.acv.service;

import com.example.acv.dto.request.CompanyInfoRequest;
import com.example.acv.dto.response.CompanyInfoResponse;
import com.example.acv.entity.CompanyInfo;
import com.example.acv.exception.ResourceNotFoundException;
import com.example.acv.repository.CompanyInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyInfoService {

    private final CompanyInfoRepository companyInfoRepository;

    @Transactional(readOnly = true)
    public CompanyInfoResponse getCurrent() {
        return toResponse(companyInfoRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("Company info not found")));
    }

    public CompanyInfoResponse save(CompanyInfoRequest request) {
        CompanyInfo companyInfo = companyInfoRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> {
                    CompanyInfo newInfo = new CompanyInfo();
                    newInfo.setCompanyName("Công ty TNHH Tư vấn và Định giá ACV");
                    return newInfo;
                });

        if (request.getCompanyName() != null) {
            companyInfo.setCompanyName(request.getCompanyName());
        }
        if (request.getIntroduction() != null) {
            companyInfo.setIntroduction(request.getIntroduction());
        }
        if (request.getHistoryTimeline() != null) {
            companyInfo.setHistoryTimeline(request.getHistoryTimeline());
        }

        if (request.getAddress() != null) {
            companyInfo.setAddress(request.getAddress());
        }
        if (request.getPhone() != null) {
            companyInfo.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            companyInfo.setEmail(request.getEmail());
        }

        return toResponse(companyInfoRepository.save(companyInfo));
    }

    private CompanyInfoResponse toResponse(CompanyInfo companyInfo) {
        return new CompanyInfoResponse(
                companyInfo.getId(),
                companyInfo.getCompanyName(),
                companyInfo.getIntroduction(),
                companyInfo.getHistoryTimeline(),
                companyInfo.getAddress(),
                companyInfo.getPhone(),
                companyInfo.getEmail(),
                companyInfo.getUpdatedAt()
        );
    }
}
