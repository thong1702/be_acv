package com.example.acv.dto.request;

import com.example.acv.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Size(max = 50)
    private String username;

    @Size(min = 6, max = 255)
    private String password;

    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 100)
    private String fullName;

    private UserRole role;

    private Integer status;
}
