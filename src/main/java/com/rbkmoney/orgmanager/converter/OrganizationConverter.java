package com.rbkmoney.orgmanager.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.swag.organizations.model.Organization;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrganizationConverter {

    private final ObjectMapper objectMapper;

    @SneakyThrows(JsonProcessingException.class)
    public OrganizationEntity toEntity(Organization organization) {
        return OrganizationEntity.builder()
                .id(organization.getId()) // TODO [a.romanov]: generated?
                .createdAt(LocalDateTime.now())
                .name(organization.getName())
                .owner(organization.getOwner()) // TODO [a.romanov]: swag fix
                .metadata(objectMapper.writeValueAsString(organization.getMetadata()))
                .build();
    }

    @SneakyThrows(JsonProcessingException.class)
    public Organization toDomain(OrganizationEntity entity) {
        Organization organization = new Organization();
        organization.setId(entity.getId());
        organization.setCreatedAt(OffsetDateTime.of(entity.getCreatedAt(), ZoneOffset.UTC));
        organization.setName(entity.getName());
        organization.setOwner(entity.getOwner());
        organization.setMetadata(objectMapper.readValue(entity.getMetadata(), Map.class));

        return organization;
    }
}
