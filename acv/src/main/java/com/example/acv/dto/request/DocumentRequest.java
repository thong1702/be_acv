package com.example.acv.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequest {

    @Size(max = 100)
    private String documentNumber;

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    @Size(max = 555)
    private String fileUrl;

    @Size(max = 10)
    private String fileType;

    @Positive
    private Long fileSize;

    private String description;
    private LocalDate issuedDate;
    private Integer status;

    @Positive
    private Long uploadedById;
}
