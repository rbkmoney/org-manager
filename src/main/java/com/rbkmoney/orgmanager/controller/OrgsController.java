package com.rbkmoney.orgmanager.controller;

import com.rbkmoney.orgmanager.service.InvitationService;
import com.rbkmoney.orgmanager.service.OrganizationRoleService;
import com.rbkmoney.orgmanager.service.OrganizationService;
import com.rbkmoney.swag.organizations.api.OrgsApi;
import com.rbkmoney.swag.organizations.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

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
        return organizationService.create(organization, xIdempotencyKey);
    }

    @Override
    public ResponseEntity<Organization> getOrg(
            String xRequestID,
            String orgId) {
        return organizationService.get(orgId);
    }

    @Override
    public ResponseEntity<Member> getOrgMember(
            String xRequestID,
            String orgId,
            String userId) {
        return organizationService.getMember(userId);
    }

    @Override
    public ResponseEntity<InlineResponse2001> listOrgMembers(String xRequestID, String orgId) {
        return organizationService.listMembers(orgId);
    }

    @Override
    public ResponseEntity<Invitation> createInvitation(
            String xRequestID,
            String orgId,
            Invitation invitation,
            String xIdempotencyKey) {
        return invitationService.create(orgId, invitation, xIdempotencyKey);
    }

    @Override
    public ResponseEntity<Invitation> getInvitation(
            String xRequestID,
            String orgId,
            String invitationId) {
        return invitationService.get(invitationId);
    }

    @Override
    public ResponseEntity<InlineResponse2002> listInvitations(String xRequestID, String orgId, InvitationStatusName status) {
        return invitationService.list(orgId, status);
    }

    @Override
    public ResponseEntity<Void> revokeInvitation(String xRequestID, String orgId, String invitationId, InlineObject1 inlineObject1) {
        return invitationService.revoke(orgId, invitationId, inlineObject1);
    }

    @Override
    public ResponseEntity<Role> getOrgRole(
            String xRequestID,
            String orgId,
            RoleId roleId) {
        return organizationRoleService.get(orgId, roleId);
    }

    @Override
    public ResponseEntity<InlineResponse200> listOrgRoles(String xRequestID, String orgId) {
        return organizationRoleService.list(orgId);
    }

    @Override
    public ResponseEntity<Organization> patchOrg(String xRequestID, String orgId, InlineObject inlineObject) {
        return null;
    }

    @Override
    public ResponseEntity<Void> assignMemberRole(
            String xRequestID,
            String orgId,
            String userId,
            MemberRole body) {
        throw new UnsupportedOperationException(); // TODO [a.romanov]: impl
    }

    @Override
    public ResponseEntity<Void> expelOrgMember(
            String xRequestID,
            String orgId,
            String userId) {
        throw new UnsupportedOperationException(); // TODO [a.romanov]: impl
    }

    @Override
    public ResponseEntity<Void> removeMemberRole(
            String xRequestID,
            String orgId,
            String userId,
            MemberRole memberRole) {
        throw new UnsupportedOperationException(); // TODO [a.romanov]: impl
    }
}
