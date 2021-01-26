package com.rbkmoney.orgmanager.controller;

import com.rbkmoney.orgmanager.service.InvitationService;
import com.rbkmoney.orgmanager.service.OrganizationRoleService;
import com.rbkmoney.orgmanager.service.OrganizationService;
import com.rbkmoney.swag.organizations.api.OrgsApi;
import com.rbkmoney.swag.organizations.model.InlineObject;
import com.rbkmoney.swag.organizations.model.InlineObject1;
import com.rbkmoney.swag.organizations.model.Invitation;
import com.rbkmoney.swag.organizations.model.InvitationListResult;
import com.rbkmoney.swag.organizations.model.InvitationStatusName;
import com.rbkmoney.swag.organizations.model.Member;
import com.rbkmoney.swag.organizations.model.MemberOrgListResult;
import com.rbkmoney.swag.organizations.model.MemberRole;
import com.rbkmoney.swag.organizations.model.Organization;
import com.rbkmoney.swag.organizations.model.Role;
import com.rbkmoney.swag.organizations.model.RoleAvailableListResult;
import com.rbkmoney.swag.organizations.model.RoleId;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OrgsController implements OrgsApi {

    private final OrganizationService organizationService;
    private final InvitationService invitationService;
    private final OrganizationRoleService organizationRoleService;

    @Override
    public ResponseEntity<Organization> createOrg(
            String xRequestID,
            Organization organization,
            String xIdempotencyKey) {
        log.info("Create organization: requestId={}, idempontencyKey={}, organization={}", xRequestID, xIdempotencyKey, organization);
        return organizationService.create(organization, xIdempotencyKey);
    }

    @Override
    public ResponseEntity<Organization> getOrg(
            String xRequestID,
            String orgId) {
        log.info("Get organization: requestId={}, orgId={}", xRequestID, orgId);
        return organizationService.get(orgId);
    }

    @Override
    public ResponseEntity<Member> getOrgMember(
            String xRequestID,
            String orgId,
            String userId) {
        log.info("Get organization: requestId={}, orgId={}, userId={}", xRequestID, orgId, userId);
        return organizationService.getMember(userId);
    }

    @Override
    public ResponseEntity<MemberOrgListResult> listOrgMembers(String xRequestID, String orgId) {
        log.info("List organization members: requestId={}, orgId={}", xRequestID, orgId);
        return organizationService.listMembers(orgId);
    }

    @Override
    public ResponseEntity<Invitation> createInvitation(
            String xRequestID,
            String orgId,
            Invitation invitation,
            String xIdempotencyKey) {
        log.info("Create invitation: requestId={}, idempontencyKey={}, orgId={}, invitation={}",
                xRequestID, xIdempotencyKey, orgId, invitation);
        return invitationService.create(orgId, invitation, xIdempotencyKey);
    }

    @Override
    public ResponseEntity<Invitation> getInvitation(
            String xRequestID,
            String orgId,
            String invitationId) {
        log.info("Get invitation: requestId={}, orgId={}, invitationId={}", xRequestID, orgId, invitationId);
        return invitationService.get(invitationId);
    }

    @Override
    public ResponseEntity<InvitationListResult> listInvitations(String xRequestID, String orgId, InvitationStatusName status) {
        log.info("List invitations: requestId={}, orgId={}, status={}", xRequestID, orgId, status);
        return invitationService.list(orgId, status);
    }

    @Override
    public ResponseEntity<Void> revokeInvitation(String xRequestID, String orgId, String invitationId, InlineObject1 inlineObject1) {
        log.info("Revoke invitation: requestId={}, orgId={}, invitationId={}, payload={}",
                xRequestID, orgId, invitationId, inlineObject1);
        return invitationService.revoke(orgId, invitationId, inlineObject1);
    }

    @Override
    public ResponseEntity<Role> getOrgRole(
            String xRequestID,
            String orgId,
            RoleId roleId) {
        log.info("Get organization id: requestId={}, orgId={}, roleId={}", xRequestID, orgId, roleId);
        return organizationRoleService.get(orgId, roleId);
    }

    @Override
    public ResponseEntity<RoleAvailableListResult> listOrgRoles(String xRequestID, String orgId) {
        log.info("List organization roles: requestId={}, orgId={}", xRequestID, orgId);
        return organizationRoleService.list(orgId);
    }

    @Override
    public ResponseEntity<Organization> patchOrg(String xRequestID, String orgId, InlineObject inlineObject) {
        return organizationService.modify(orgId, inlineObject.getName());
    }

    @Override
    public ResponseEntity<Void> assignMemberRole(
            String xRequestID,
            String orgId,
            String userId,
            MemberRole body) {
        log.info("Assign member role: requestId={}, orgId={}, payload={}", xRequestID, orgId, body);
        return organizationService.assignMemberRole(orgId, userId, body);
    }

    @Override
    public ResponseEntity<Void> expelOrgMember(
            String xRequestID,
            String orgId,
            String userId) {
        log.info("Expel member organization: requestId={}, orgId={}, userId={}", xRequestID, orgId, userId);
        return organizationService.expelOrgMember(orgId, userId);
    }

    @Override
    public ResponseEntity<Void> removeMemberRole(
            String xRequestID,
            String orgId,
            String userId,
            MemberRole memberRole) {
        log.info("Expel member organization: requestId={}, orgId={}, userId={}", xRequestID, orgId, userId);
        return organizationService.removeMemberRole(orgId, userId, memberRole);
    }
}
