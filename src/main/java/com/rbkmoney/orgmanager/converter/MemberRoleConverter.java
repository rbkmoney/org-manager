package com.rbkmoney.orgmanager.converter;

import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.swag.organizations.model.MemberRole;
import com.rbkmoney.swag.organizations.model.MemberRoleScope;
import com.rbkmoney.swag.organizations.model.ResourceScopeId;
import com.rbkmoney.swag.organizations.model.RoleId;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MemberRoleConverter {

    public MemberRoleEntity toEntity(MemberRole role, String orgId) {
        return MemberRoleEntity.builder()
                .id(UUID.randomUUID().toString())
                .organizationId(orgId)
                .resourceId(role.getScope().getResourceId())
                .roleId(role.getRoleId().getValue())
                .scopeId(role.getScope().getId().getValue())
                .build();
    }

    public MemberRole toDomain(MemberRoleEntity entity) {
        return new MemberRole()
                .roleId(RoleId.fromValue(entity.getRoleId()))
                .scope(new MemberRoleScope()
                        .id(ResourceScopeId.fromValue(entity.getScopeId()))
                        .resourceId(entity.getResourceId()));
    }
}
