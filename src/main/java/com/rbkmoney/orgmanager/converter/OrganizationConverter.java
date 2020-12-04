package com.rbkmoney.orgmanager.converter;

import com.rbkmoney.bouncer.context.v1.Entity;
import com.rbkmoney.bouncer.context.v1.OrgRole;
import com.rbkmoney.bouncer.context.v1.OrgRoleScope;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.util.JsonMapper;
import com.rbkmoney.swag.organizations.model.Organization;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationConverter {

    private final JsonMapper jsonMapper;

    public OrganizationEntity toEntity(Organization organization) {
        return OrganizationEntity.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .name(organization.getName())
                .owner(organization.getOwner().toString())
                .metadata(jsonMapper.toJson(organization.getMetadata()))
                .build();
    }

    public Organization toDomain(OrganizationEntity entity) {
        return new Organization()
                .id(entity.getId())
                .createdAt(OffsetDateTime.of(entity.getCreatedAt(), ZoneOffset.UTC))
                .name(entity.getName())
                .owner(entity.getOwner())
                .metadata(entity.getMetadata() != null ? jsonMapper.toMap(entity.getMetadata()) : null);
    }

    public com.rbkmoney.bouncer.context.v1.Organization toThrift(OrganizationEntity e) {
        return new com.rbkmoney.bouncer.context.v1.Organization()
                .setId(e.getId())
                .setOwner(new Entity())
                .setRoles(e.getRoles() == null ? null :
                        e.getRoles().stream()
                                .map(r -> new OrgRole()
                                        .setId(r.getId())
                                        .setScope(new OrgRoleScope()
                                                .setShop(new Entity())))
                                .collect(Collectors.toSet()));
    }
}
