package com.rbkmoney.orgmanager.controller;

import com.rbkmoney.orgmanager.service.InvitationService;
import com.rbkmoney.orgmanager.service.KeycloakService;
import com.rbkmoney.orgmanager.service.OrganizationRoleService;
import com.rbkmoney.orgmanager.service.OrganizationService;
import com.rbkmoney.orgmanager.service.ResourceAccessService;
import com.rbkmoney.swag.organizations.api.OrgsApi;
import com.rbkmoney.swag.organizations.model.InlineObject;
import com.rbkmoney.swag.organizations.model.InlineObject1;
import com.rbkmoney.swag.organizations.model.Invitation;
import com.rbkmoney.swag.organizations.model.InvitationListResult;
import com.rbkmoney.swag.organizations.model.InvitationRequest;
import com.rbkmoney.swag.organizations.model.InvitationStatusName;
import com.rbkmoney.swag.organizations.model.Member;
import com.rbkmoney.swag.organizations.model.MemberOrgListResult;
import com.rbkmoney.swag.organizations.model.MemberRole;
import com.rbkmoney.swag.organizations.model.Organization;
import com.rbkmoney.swag.organizations.model.Role;
import com.rbkmoney.swag.organizations.model.RoleAvailableListResult;
import com.rbkmoney.swag.organizations.model.RoleId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.AccessToken;
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
            String xRequestID,
            Organization organization,
            String xIdempotencyKey) {
        log.info("Create organization: requestId={}, idempontencyKey={}, organization={}", xRequestID, xIdempotencyKey,
                organization);
        resourceAccessService.checkRights();
        AccessToken accessToken = keycloakService.getAccessToken();
        return organizationService.create(accessToken.getSubject(), organization, xIdempotencyKey);
    }

    @Override
    public ResponseEntity<Organization> getOrg(
            String xRequestID,
            String orgId) {
        log.info("Get organization: requestId={}, orgId={}", xRequestID, orgId);
        resourceAccessService.checkOrganizationRights(orgId);
        return organizationService.get(orgId);
    }

    @Override
    public ResponseEntity<Member> getOrgMember(
            String xRequestID,
            String orgId,
            String userId) {
        log.info("Get organization member: requestId={}, orgId={}, userId={}", xRequestID, orgId, userId);
        resourceAccessService.checkMemberRights(orgId, userId);
        return organizationService.getMember(userId);
    }

    @Override
    public ResponseEntity<MemberOrgListResult> listOrgMembers(String xRequestID, String orgId) {
        log.info("List organization members: requestId={}, orgId={}", xRequestID, orgId);
        resourceAccessService.checkOrganizationRights(orgId);
        return organizationService.listMembers(orgId);
    }

    @Override
    public ResponseEntity<Invitation> createInvitation(String xRequestID,
                                                       @Size(min = 1, max = 40) String orgId,
                                                       @Valid InvitationRequest invitationRequest,
                                                       String xIdempotencyKey) {
        log.info("Create invitation: requestId={}, idempontencyKey={}, orgId={}, invitation={}",
                xRequestID, xIdempotencyKey, orgId, invitationRequest);
        resourceAccessService.checkInvitationRights(orgId, invitationRequest);
        return invitationService.create(orgId, invitationRequest, xIdempotencyKey);
    }

    @Override
    public ResponseEntity<Invitation> getInvitation(
            String xRequestID,
            String orgId,
            String invitationId) {
        log.info("Get invitation: requestId={}, orgId={}, invitationId={}", xRequestID, orgId, invitationId);
        resourceAccessService.checkInvitationRights(orgId, invitationId);
        return invitationService.get(invitationId);
    }

    @Override
    public ResponseEntity<InvitationListResult> listInvitations(String xRequestID, String orgId, InvitationStatusName status) {
        log.info("List invitations: requestId={}, orgId={}, status={}", xRequestID, orgId, status);
        resourceAccessService.checkOrganizationRights(orgId);
        return invitationService.list(orgId, status);
    }

    @Override
    public ResponseEntity<Void> revokeInvitation(String xRequestID, String orgId, String invitationId, InlineObject1 inlineObject1) {
        log.info("Revoke invitation: requestId={}, orgId={}, invitationId={}, payload={}",
                xRequestID, orgId, invitationId, inlineObject1);
        resourceAccessService.checkInvitationRights(orgId, invitationId);
        return invitationService.revoke(orgId, invitationId, inlineObject1);
    }

    @Override
    public ResponseEntity<Role> getOrgRole(
            String xRequestID,
            String orgId,
            RoleId roleId) {
        log.info("Get organization id: requestId={}, orgId={}, roleId={}", xRequestID, orgId, roleId);
        MemberRole memberRole = new MemberRole();
        memberRole.setRoleId(roleId);
        resourceAccessService.checkRoleRights(orgId, memberRole);
        return organizationRoleService.get(orgId, roleId);
    }

    @Override
    public ResponseEntity<RoleAvailableListResult> listOrgRoles(String xRequestID, String orgId) {
        log.info("List organization roles: requestId={}, orgId={}", xRequestID, orgId);
        resourceAccessService.checkOrganizationRights(orgId);
        return organizationRoleService.list(orgId);
    }

    @Override
    public ResponseEntity<Organization> patchOrg(String xRequestID, String orgId, InlineObject inlineObject) {
        resourceAccessService.checkOrganizationRights(orgId);
        return organizationService.modify(orgId, inlineObject.getName());
    }

    @Override
    public ResponseEntity<Void> assignMemberRole(
            String xRequestID,
            String orgId,
            String userId,
            MemberRole body) {
        log.info("Assign member role: requestId={}, orgId={}, payload={}", xRequestID, orgId, body);
        resourceAccessService.checkMemberRoleRights(orgId, userId, body);
        return organizationService.assignMemberRole(orgId, userId, body);
    }

    @Override
    public ResponseEntity<Void> expelOrgMember(
            String xRequestID,
            String orgId,
            String userId) {
        log.info("Expel member organization: requestId={}, orgId={}, userId={}", xRequestID, orgId, userId);
        resourceAccessService.checkMemberRights(orgId, userId);
        return organizationService.expelOrgMember(orgId, userId);
    }

    @Override
    public ResponseEntity<Void> removeMemberRole(
            String xRequestID,
            String orgId,
            String userId,
            MemberRole memberRole) {
        log.info("Expel member organization: requestId={}, orgId={}, userId={}", xRequestID, orgId, userId);
        resourceAccessService.checkMemberRoleRights(orgId, userId, memberRole);
        return organizationService.removeMemberRole(orgId, userId, memberRole);
    }
}
