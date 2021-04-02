package com.rbkmoney.orgmanager.converter;

import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.swag.organizations.model.MemberRole;
import com.rbkmoney.swag.organizations.model.MemberRoleScope;
import com.rbkmoney.swag.organizations.model.ResourceScopeId;
import com.rbkmoney.swag.organizations.model.RoleId;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class MemberRoleConverter {

    public MemberRoleEntity toEntity(MemberRole role, String orgId) {
        MemberRoleScope scope = role.getScope();
        return MemberRoleEntity.builder()
                .id(UUID.randomUUID().toString())
                .organizationId(orgId)
                .resourceId(Objects.nonNull(scope) ? scope.getResourceId() : null)
                .roleId(role.getRoleId().toString())
                .scopeId(Objects.nonNull(scope) ? scope.getId().toString() : null)
                .build();
    }

    public MemberRole toDomain(MemberRoleEntity entity) {
        MemberRoleScope memberRoleScope = Optional.ofNullable(entity.getScopeId())
                .map(ResourceScopeId::fromValue)
                .map(resourceScopeId -> new MemberRoleScope()
                        .id(resourceScopeId)
                        .resourceId(entity.getResourceId()))
                .orElse(null);
        return new MemberRole()
                .id(entity.getId())
                .roleId(RoleId.fromValue(entity.getRoleId()))
                .scope(memberRoleScope);
    }
}
