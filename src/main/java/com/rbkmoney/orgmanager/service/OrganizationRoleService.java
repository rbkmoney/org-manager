package com.rbkmoney.orgmanager.service;

import static java.util.stream.Collectors.toList;

import com.rbkmoney.orgmanager.converter.OrganizationRoleConverter;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.entity.OrganizationRoleEntity;
import com.rbkmoney.orgmanager.repository.OrganizationRepository;
import com.rbkmoney.orgmanager.repository.OrganizationRoleRepository;
import com.rbkmoney.swag.organizations.model.Role;
import com.rbkmoney.swag.organizations.model.RoleAvailableListResult;
import com.rbkmoney.swag.organizations.model.RoleId;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationRoleService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationRoleRepository organizationRoleRepository;
    private final OrganizationRoleConverter organizationRoleConverter;

    public ResponseEntity<Role> get(String orgId, RoleId roleId) {
        boolean isOrganizationExists = organizationRepository.existsById(orgId);

        if (!isOrganizationExists) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Optional<OrganizationRoleEntity> entity = organizationRoleRepository.findByOrganizationIdAndRoleId(orgId, roleId.toString());

        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Role role = organizationRoleConverter.toDomain(entity.get());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(role);
    }

    public ResponseEntity<RoleAvailableListResult> list(String orgId) {
        Optional<OrganizationEntity> entity = organizationRepository.findById(orgId);

        if (entity.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        List<Role> roles = entity.get().getRoles()
                .stream()
                .map(organizationRoleConverter::toDomain)
                .collect(toList());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new RoleAvailableListResult()
                        .result(roles));
    }
}
