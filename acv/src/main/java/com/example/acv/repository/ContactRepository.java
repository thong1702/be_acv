package com.example.acv.repository;

import com.example.acv.entity.ContactRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<ContactRequest, Long> {

    @Query("SELECT c FROM ContactRequest c WHERE " +
           "(:search IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR c.status = :status) " +
           "ORDER BY c.createdAt DESC")
    Page<ContactRequest> searchRequests(@Param("search") String search,
                                        @Param("status") Integer status,
                                        Pageable pageable);

    long countByStatus(Integer status);
}
