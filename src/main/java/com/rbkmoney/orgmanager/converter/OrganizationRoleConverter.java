package com.rbkmoney.orgmanager.converter;

import com.rbkmoney.orgmanager.entity.OrganizationRoleEntity;
import com.rbkmoney.swag.organizations.model.ResourceScopeId;
import com.rbkmoney.swag.organizations.model.Role;
import com.rbkmoney.swag.organizations.model.RoleId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationRoleConverter {

    public Role toDomain(OrganizationRoleEntity entity) {
        return new Role()
                .id(RoleId.fromValue(entity.getRoleId()))
                .name(entity.getName())
                .scopes(entity.getPossibleScopes()
                        .stream()
                        .map(s -> ResourceScopeId.fromValue(s.getId()))
                        .collect(Collectors.toList()));
    }
}