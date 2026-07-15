package com.example.acv.controller;

import com.example.acv.dto.request.OrganizationNodeRequest;
import com.example.acv.dto.response.OrganizationNodeResponse;
import com.example.acv.service.OrganizationNodeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organization-nodes")
@RequiredArgsConstructor
public class OrganizationNodeController {

    private final OrganizationNodeService organizationNodeService;

    @GetMapping
    public List<OrganizationNodeResponse> getAll() {
        return organizationNodeService.getAll();
    }

    @GetMapping("/{id}")
    public OrganizationNodeResponse getById(@PathVariable Integer id) {
        return organizationNodeService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationNodeResponse create(@Valid @RequestBody OrganizationNodeRequest request) {
        return organizationNodeService.create(request);
    }

    @PutMapping("/{id}")
    public OrganizationNodeResponse update(@PathVariable Integer id, @Valid @RequestBody OrganizationNodeRequest request) {
        return organizationNodeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        organizationNodeService.delete(id);
    }
}
