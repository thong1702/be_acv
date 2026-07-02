package com.example.cms.service;

import com.example.cms.dto.request.CompanyInfoRequest;
import com.example.cms.dto.response.CompanyInfoResponse;
import com.example.cms.entity.CompanyInfo;
import com.example.cms.exception.ResourceNotFoundException;
import com.example.cms.repository.CompanyInfoRepository;
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
                .orElseGet(CompanyInfo::new);

        companyInfo.setCompanyName(request.getCompanyName());
        companyInfo.setIntroduction(request.getIntroduction());
        companyInfo.setHistoryTimeline(request.getHistoryTimeline());
        companyInfo.setOrganizationChart(request.getOrganizationChart());
        companyInfo.setAddress(request.getAddress());
        companyInfo.setPhone(request.getPhone());
        companyInfo.setEmail(request.getEmail());

        return toResponse(companyInfoRepository.save(companyInfo));
    }

    private CompanyInfoResponse toResponse(CompanyInfo companyInfo) {
        return new CompanyInfoResponse(
                companyInfo.getId(),
                companyInfo.getCompanyName(),
                companyInfo.getIntroduction(),
                companyInfo.getHistoryTimeline(),
                companyInfo.getOrganizationChart(),
                companyInfo.getAddress(),
                companyInfo.getPhone(),
                companyInfo.getEmail(),
                companyInfo.getUpdatedAt()
        );
    }
}
