package com.example.acv.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contact_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String phone;

    private String email;

    private String serviceType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @Column(nullable = false)
    private Integer status = 0; // 0: Chưa phản hồi, 1: Đã phản hồi

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        }
        if (status == null) {
            status = 0;
        }
    }
}
