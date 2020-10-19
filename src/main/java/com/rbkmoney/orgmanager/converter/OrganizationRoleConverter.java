package com.rbkmoney.orgmanager.converter;

import com.rbkmoney.orgmanager.entity.OrganizationRoleEntity;
import com.rbkmoney.swag.organizations.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationRoleConverter {

    public Role toDomain(OrganizationRoleEntity entity) {
        return null; // TODO [a.romanov]: impl
    }
}