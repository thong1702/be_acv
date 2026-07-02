package com.example.cms.controller;

import com.example.cms.dto.request.CompanyInfoRequest;
import com.example.cms.dto.response.CompanyInfoResponse;
import com.example.cms.service.CompanyInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/company-info")
@RequiredArgsConstructor
public class CompanyInfoController {

    private final CompanyInfoService companyInfoService;

    @GetMapping
    public CompanyInfoResponse getCurrent() {
        return companyInfoService.getCurrent();
    }

    @PutMapping
    public CompanyInfoResponse save(@Valid @RequestBody CompanyInfoRequest request) {
        return companyInfoService.save(request);
    }
}
