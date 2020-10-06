package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.converter.OrganizationConverter;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.repository.OrganizationRepository;
import com.rbkmoney.swag.organizations.model.Organization;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationConverter organizationConverter;
    private final OrganizationRepository organizationRepository;

    // TODO [a.romanov]: idempotency
    public ResponseEntity<Organization> create(
            Organization organization,
            String xIdempotencyKey) {
        OrganizationEntity entity = organizationConverter.toEntity(organization);
        organizationRepository.save(entity);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(organization);
    }

    public ResponseEntity<Organization> get(String orgId) {
        Optional<OrganizationEntity> entity = organizationRepository.findById(orgId);

        if (entity.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Organization organization = organizationConverter.toDomain(entity.get());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(organization);
    }
}
