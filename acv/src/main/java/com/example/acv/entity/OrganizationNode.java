package com.example.acv.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "organization_nodes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String position;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "parent_id")
    private Integer parentId;

    @Column(length = 255)
    private String degree;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(length = 10)
    private String gender;

    @Column(name = "birth_year")
    private Integer birthYear;

    @Column(name = "certificate_no", length = 100)
    private String certificateNo;

    @Column(name = "personnel_group", length = 50)
    private String personnelGroup;

    @Column(name = "work_history", columnDefinition = "TEXT")
    private String workHistory;

    @Column(name = "key_experience", columnDefinition = "TEXT")
    private String keyExperience;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        updatedAt = LocalDateTime.now();
        if (orderIndex == null) {
            orderIndex = 0;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
