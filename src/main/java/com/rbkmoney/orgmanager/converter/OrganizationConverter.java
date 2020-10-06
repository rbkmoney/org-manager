package com.rbkmoney.orgmanager.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.util.JsonMapper;
import com.rbkmoney.swag.organizations.model.Organization;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationConverter {

    private final JsonMapper jsonMapper;

    public OrganizationEntity toEntity(Organization organization) {
        return OrganizationEntity.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .name(organization.getName())
                .owner(organization.getOwner())
                .metadata(jsonMapper.toJson(organization.getMetadata()))
                .build();
    }

    @SneakyThrows(JsonProcessingException.class)
    public Organization toDomain(OrganizationEntity entity) {
        return new Organization()
                .id(entity.getId())
                .createdAt(OffsetDateTime.of(entity.getCreatedAt(), ZoneOffset.UTC))
                .name(entity.getName())
                .owner(entity.getOwner())
                .metadata(jsonMapper.toMap(entity.getMetadata()));
    }
}
