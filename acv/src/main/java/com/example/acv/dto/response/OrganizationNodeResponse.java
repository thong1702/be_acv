package com.example.acv.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrganizationNodeResponse {

    private Integer id;
    private String name;
    private String position;
    private String description;
    private String avatarUrl;
    private String email;
    private String phone;
    private Integer orderIndex;
    private Integer parentId;
    private String parentName;
    private String degree;
    private Integer experienceYears;
    private String gender;
    private Integer birthYear;
    private String certificateNo;
    private String personnelGroup;
    private String workHistory;
    private String keyExperience;
    private LocalDateTime updatedAt;
}
