package com.example.acv.repository;

import com.example.acv.entity.OrganizationNode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationNodeRepository extends JpaRepository<OrganizationNode, Integer> {
    List<OrganizationNode> findAllByOrderByOrderIndexAscIdAsc();
}
