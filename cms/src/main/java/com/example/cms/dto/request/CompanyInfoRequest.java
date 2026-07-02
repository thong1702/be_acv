package com.example.cms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyInfoRequest {

    @NotBlank
    @Size(max = 255)
    private String companyName;

    private String introduction;
    private String historyTimeline;
    private String organizationChart;

    @Size(max = 255)
    private String address;

    @Size(max = 20)
    private String phone;

    @Size(max = 100)
    private String email;
}
