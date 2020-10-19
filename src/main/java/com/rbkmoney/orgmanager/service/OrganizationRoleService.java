package com.rbkmoney.orgmanager.service;

import com.rbkmoney.swag.organizations.model.InlineResponse2001;
import com.rbkmoney.swag.organizations.model.Role;
import com.rbkmoney.swag.organizations.model.RoleId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationRoleService {

    public ResponseEntity<Role> get(String orgId, RoleId roleId) {
        return null;
    }

    public ResponseEntity<InlineResponse2001> list(String orgId) {
        return null;
    }
}
