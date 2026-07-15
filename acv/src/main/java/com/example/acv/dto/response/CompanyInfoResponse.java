package com.example.acv.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CompanyInfoResponse {

    private Integer id;
    private String companyName;
    private String introduction;
    private String historyTimeline;
    private String address;
    private String phone;
    private String email;
    private LocalDateTime updatedAt;
}
