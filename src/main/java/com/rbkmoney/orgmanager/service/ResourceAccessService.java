package com.rbkmoney.orgmanager.service;

import com.rbkmoney.swag.organizations.model.MemberRole;
import com.rbkmoney.swag.organizations.model.OrganizationJoinRequest;

public interface ResourceAccessService {

    void checkRights();

    void checkOrganizationRights(String orgId);

    void checkOrganizationRights(OrganizationJoinRequest request);

    void checkMemberRights(String orgId, String memberId);

    void checkRoleRights(String orgId, MemberRole memberRole);

    void checkMemberRoleRights(String orgId, String memberId, MemberRole memberRole);

}
