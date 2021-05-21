package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.service.dto.ResourceDto;
import com.rbkmoney.swag.organizations.model.InvitationRequest;
import com.rbkmoney.swag.organizations.model.MemberRole;
import com.rbkmoney.swag.organizations.model.OrganizationJoinRequest;

public interface ResourceAccessService {

    void checkRights();

    void checkRights(ResourceDto resource);

    void checkOrganizationRights(String orgId);

    void checkOrganizationRights(OrganizationJoinRequest request);

    void checkMemberRights(String orgId, String memberId);

    void checkRoleRights(String orgId, MemberRole memberRole);

    void checkMemberRoleRights(String orgId, String memberId, MemberRole memberRole);

    void checkMemberRoleRights(String orgId, String memberId, String memberRoleId);

    void checkInvitationRights(String orgId, InvitationRequest invitationRequest);

    void checkInvitationRights(String orgId, String invitationId);

}
