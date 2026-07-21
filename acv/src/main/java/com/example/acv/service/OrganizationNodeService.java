package com.example.acv.service;

import com.example.acv.dto.request.OrganizationNodeRequest;
import com.example.acv.dto.response.OrganizationNodeResponse;
import com.example.acv.entity.OrganizationNode;
import com.example.acv.exception.BadRequestException;
import com.example.acv.exception.ResourceNotFoundException;
import com.example.acv.repository.OrganizationNodeRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationNodeService {

    private final OrganizationNodeRepository organizationNodeRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public List<OrganizationNodeResponse> getAll() {
        return organizationNodeRepository.findAllByOrderByOrderIndexAscIdAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrganizationNodeResponse getById(Integer id) {
        return organizationNodeRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Organization node not found with id: " + id));
    }

    public OrganizationNodeResponse create(OrganizationNodeRequest request) {
        OrganizationNode node = OrganizationNode.builder()
                .name(request.getName())
                .position(request.getPosition())
                .description(request.getDescription())
                .avatarUrl(request.getAvatarUrl())
                .email(request.getEmail())
                .phone(request.getPhone())
                .orderIndex(request.getOrderIndex() == null ? 0 : request.getOrderIndex())
                .parentId(request.getParentId())
                .build();

        return toResponse(organizationNodeRepository.save(node));
    }

    public OrganizationNodeResponse update(Integer id, OrganizationNodeRequest request) {
        OrganizationNode node = organizationNodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization node not found with id: " + id));

        node.setName(request.getName());
        node.setPosition(request.getPosition());
        node.setDescription(request.getDescription());
        if (request.getAvatarUrl() != null) {
            String oldAvatar = node.getAvatarUrl();
            if (oldAvatar != null && !oldAvatar.equals(request.getAvatarUrl())) {
                cloudinaryService.deleteFileByUrl(oldAvatar);
            }
        }
        node.setAvatarUrl(request.getAvatarUrl());
        node.setEmail(request.getEmail());
        node.setPhone(request.getPhone());
        node.setOrderIndex(request.getOrderIndex() == null ? 0 : request.getOrderIndex());
        node.setParentId(request.getParentId());

        return toResponse(organizationNodeRepository.save(node));
    }

    public void delete(Integer id) {
        OrganizationNode node = organizationNodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization node not found with id: " + id));
        if (node.getAvatarUrl() != null) {
            cloudinaryService.deleteFileByUrl(node.getAvatarUrl());
        }
        organizationNodeRepository.delete(node);
    }

    private OrganizationNodeResponse toResponse(OrganizationNode node) {
        return new OrganizationNodeResponse(
                node.getId(),
                node.getName(),
                node.getPosition(),
                node.getDescription(),
                node.getAvatarUrl(),
                node.getEmail(),
                node.getPhone(),
                node.getOrderIndex(),
                node.getParentId(),
                null,
                node.getUpdatedAt()
        );
    }
}
