package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.converter.MemberConverter;
import com.rbkmoney.orgmanager.converter.OrganizationConverter;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.repository.OrganizationRepository;
import com.rbkmoney.swag.organizations.model.InlineResponse2002;
import com.rbkmoney.swag.organizations.model.Member;
import com.rbkmoney.swag.organizations.model.Organization;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationConverter organizationConverter;
    private final MemberConverter memberConverter;
    private final OrganizationRepository organizationRepository;

    // TODO [a.romanov]: idempotency
    public ResponseEntity<Organization> create(
            Organization organization,
            String xIdempotencyKey) {
        OrganizationEntity entity = organizationConverter.toEntity(organization);
        OrganizationEntity savedEntity = organizationRepository.save(entity);

        Organization savedOrganization = organizationConverter.toDomain(savedEntity);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedOrganization);
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

    public ResponseEntity<InlineResponse2002> listMembers(String orgId) {
        Optional<OrganizationEntity> entity = organizationRepository.findById(orgId);

        if (entity.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        List<Member> members = entity.get().getMembers()
                .stream()
                .map(memberConverter::toDomain)
                .collect(toList());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new InlineResponse2002()
                        .results(members));

    }
}
