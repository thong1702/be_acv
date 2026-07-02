package com.example.cms.repository;

import com.example.cms.entity.CompanyInfo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyInfoRepository extends JpaRepository<CompanyInfo, Integer> {

    Optional<CompanyInfo> findFirstByOrderByIdAsc();
}
