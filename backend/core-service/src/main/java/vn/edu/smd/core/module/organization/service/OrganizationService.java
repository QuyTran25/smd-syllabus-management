package vn.edu.smd.core.module.organization.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.Organization;
import vn.edu.smd.core.module.organization.dto.OrganizationRequest;
import vn.edu.smd.core.module.organization.dto.OrganizationResponse;
import vn.edu.smd.core.repository.OrganizationRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public List<OrganizationResponse> getAllOrganizations() {
        return organizationRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OrganizationResponse getOrganizationById(UUID id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        return mapToResponse(organization);
    }

    @Transactional
    public OrganizationResponse createOrganization(OrganizationRequest request) {
        Organization organization = new Organization();
        organization.setName(request.getName());
        organization.setCode(request.getCode());
        organization.setAddress(request.getAddress());
        organization.setPhone(request.getPhoneNumber());
        organization.setEmail(request.getEmail());
        organization.setWebsite(request.getWebsite());

        Organization savedOrganization = organizationRepository.save(organization);
        return mapToResponse(savedOrganization);
    }

    @Transactional
    public OrganizationResponse updateOrganization(UUID id, OrganizationRequest request) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));

        organization.setName(request.getName());
        organization.setCode(request.getCode());
        organization.setAddress(request.getAddress());
        organization.setPhone(request.getPhoneNumber());
        organization.setEmail(request.getEmail());
        organization.setWebsite(request.getWebsite());

        Organization updatedOrganization = organizationRepository.save(organization);
        return mapToResponse(updatedOrganization);
    }

    @Transactional
    public void deleteOrganization(UUID id) {
        if (!organizationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Organization", "id", id);
        }
        organizationRepository.deleteById(id);
    }

    private OrganizationResponse mapToResponse(Organization organization) {
        OrganizationResponse response = new OrganizationResponse();
        response.setId(organization.getId());
        response.setName(organization.getName());
        response.setCode(organization.getCode());
        response.setAddress(organization.getAddress());
        response.setPhoneNumber(organization.getPhone());
        response.setEmail(organization.getEmail());
        response.setWebsite(organization.getWebsite());
        response.setCreatedAt(organization.getCreatedAt());
        response.setUpdatedAt(organization.getUpdatedAt());
        return response;
    }
}
