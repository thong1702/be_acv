package com.example.cms.dto.response;

import com.example.cms.entity.UserRole;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private UserRole role;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
