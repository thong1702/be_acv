package com.example.acv.repository;

import com.example.acv.entity.CompanyInfo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyInfoRepository extends JpaRepository<CompanyInfo, Integer> {

    Optional<CompanyInfo> findFirstByOrderByIdAsc();
}
