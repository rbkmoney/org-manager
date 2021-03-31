package com.rbkmoney.orgmanager.service;

import com.rbkmoney.swag.organizations.model.MemberRole;

public interface MemberRoleService {

    MemberRole findById(String id);

    void delete(String id);

}
