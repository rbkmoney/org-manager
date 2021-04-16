package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.swag.organizations.model.MemberRole;

public interface MemberRoleService {

    MemberRole findById(String id);

    MemberRoleEntity findEntityById(String id);


}
