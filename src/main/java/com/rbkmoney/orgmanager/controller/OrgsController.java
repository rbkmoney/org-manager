package com.rbkmoney.orgmanager.controller;

import com.rbkmoney.orgmanager.service.*;
import com.rbkmoney.swag.organizations.api.OrgsApi;
import com.rbkmoney.swag.organizations.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.AccessToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Size;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OrgsController implements OrgsApi {

    private final OrganizationService organizationService;
    private final InvitationService invitationService;
    private final OrganizationRoleService organizationRoleService;
    private final KeycloakService keycloakService;
    private final ResourceAccessService resourceAccessService;

    @Override
    public ResponseEntity<Organization> createOrg(
            String requestId,
            Organization organization,
            String idempotencyKey) {
        log.info("Create organization: requestId={}, idempotencyKey={}, organization={}", requestId, idempotencyKey,
                organization);
        resourceAccessService.checkRights();
        AccessToken accessToken = keycloakService.getAccessToken();
        return organizationService.create(accessToken.getSubject(), organization, idempotencyKey);
    }

    @Override
    public ResponseEntity<Organization> getOrg(
            String requestId,
            String orgId) {
        log.info("Get organization: requestId={}, orgId={}", requestId, orgId);
        resourceAccessService.checkOrganizationRights(orgId);
        return organizationService.get(orgId);
    }

    @Override
    public ResponseEntity<Member> getOrgMember(
            String requestId,
            String orgId,
            String userId) {
        log.info("Get organization member: requestId={}, orgId={}, userId={}", requestId, orgId, userId);
        resourceAccessService.checkMemberRights(orgId, userId);
        return ResponseEntity.ok(organizationService.getOrgMember(userId, orgId));
    }

    @Override
    public ResponseEntity<MemberOrgListResult> listOrgMembers(String requestId, String orgId) {
        log.info("List organization members: requestId={}, orgId={}", requestId, orgId);
        resourceAccessService.checkOrganizationRights(orgId);
        return ResponseEntity.ok(organizationService.listMembers(orgId));
    }

    @Override
    public ResponseEntity<Invitation> createInvitation(String requestId,
                                                       @Size(min = 1, max = 40) String orgId,
                                                       @Valid InvitationRequest invitationRequest,
                                                       String idempotencyKey) {
        log.info("Create invitation: requestId={}, idempotencyKey={}, orgId={}, invitation={}",
                requestId, idempotencyKey, orgId, invitationRequest);
        resourceAccessService.checkInvitationRights(orgId, invitationRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(invitationService.create(orgId, invitationRequest, idempotencyKey));
    }

    @Override
    public ResponseEntity<Invitation> getInvitation(
            String requestId,
            String orgId,
            String invitationId) {
        log.info("Get invitation: requestId={}, orgId={}, invitationId={}", requestId, orgId, invitationId);
        resourceAccessService.checkInvitationRights(orgId, invitationId);
        return invitationService.get(invitationId);
    }

    @Override
    public ResponseEntity<InvitationListResult> listInvitations(String requestId,
                                                                String orgId,
                                                                InvitationStatusName status) {
        log.info("List invitations: requestId={}, orgId={}, status={}", requestId, orgId, status);
        resourceAccessService.checkOrganizationRights(orgId);
        return invitationService.list(orgId, status);
    }

    @Override
    public ResponseEntity<Void> revokeInvitation(String requestId,
                                                 String orgId,
                                                 String invitationId,
                                                 InlineObject1 inlineObject1) {
        log.info("Revoke invitation: requestId={}, orgId={}, invitationId={}, payload={}",
                requestId, orgId, invitationId, inlineObject1);
        resourceAccessService.checkInvitationRights(orgId, invitationId);
        invitationService.revoke(orgId, invitationId, inlineObject1);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @Override
    public ResponseEntity<Role> getOrgRole(
            String requestId,
            String orgId,
            RoleId roleId) {
        log.info("Get organization id: requestId={}, orgId={}, roleId={}", requestId, orgId, roleId);
        MemberRole memberRole = new MemberRole();
        memberRole.setRoleId(roleId);
        resourceAccessService.checkRoleRights(orgId, memberRole);
        return ResponseEntity.ok(organizationRoleService.get(orgId, roleId));
    }

    @Override
    public ResponseEntity<RoleAvailableListResult> listOrgRoles(String requestId, String orgId) {
        log.info("List organization roles: requestId={}, orgId={}", requestId, orgId);
        resourceAccessService.checkOrganizationRights(orgId);
        return organizationRoleService.list(orgId);
    }

    @Override
    public ResponseEntity<Organization> patchOrg(String requestId, String orgId, InlineObject inlineObject) {
        resourceAccessService.checkOrganizationRights(orgId);
        return organizationService.modify(orgId, inlineObject.getName());
    }

    @Override
    public ResponseEntity<MemberRole> assignMemberRole(
            String requestId,
            String orgId,
            String userId,
            MemberRole body) {
        log.info("Assign member role: requestId={}, orgId={}, payload={}", requestId, orgId, body);
        resourceAccessService.checkMemberRoleRights(orgId, userId, body);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(organizationService.assignMemberRole(orgId, userId, body));
    }

    @Override
    public ResponseEntity<Void> expelOrgMember(
            String requestId,
            String orgId,
            String userId) {
        log.info("Expel member organization: requestId={}, orgId={}, userId={}", requestId, orgId, userId);
        resourceAccessService.checkMemberRights(orgId, userId);
        organizationService.expelOrgMember(orgId, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> removeMemberRole(
            String requestId,
            String orgId,
            String userId,
            String memberRoleId) {
        log.info("Remove member role: requestId={}, orgId={}, userId={}, memberRoleId={}", requestId, orgId,
                userId, memberRoleId);
        resourceAccessService.checkMemberRoleRights(orgId, userId, memberRoleId);
        organizationService.removeMemberRole(orgId, userId, memberRoleId);
        return ResponseEntity.noContent().build();
    }
}
