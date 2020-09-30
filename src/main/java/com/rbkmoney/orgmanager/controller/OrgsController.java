package com.rbkmoney.orgmanager.controller;

import com.rbkmoney.swag.organizations.api.OrgsApi;
import com.rbkmoney.swag.organizations.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrgsController implements OrgsApi {

    @Override
    public ResponseEntity<Void> assignMemberRole(
            String xRequestID,
            String orgId,
            String userId,
            MemberRole body) {
        return null;
    }

    @Override
    public ResponseEntity<Invitation> createInvitation(
            String xRequestID,
            String orgId,
            Invitation body,
            String xIdempotencyKey) {
        return null;
    }

    @Override
    public ResponseEntity<Organization> createOrg(
            String xRequestID,
            Organization body,
            String xIdempotencyKey) {
        return null;
    }

    @Override
    public ResponseEntity<Void> expelOrgMember(
            String xRequestID,
            String orgId,
            String userId) {
        return null;
    }

    @Override
    public ResponseEntity<Invitation> getInvitation(
            String xRequestID,
            String orgId,
            String invitationId) {
        return null;
    }

    @Override
    public ResponseEntity<Organization> getOrg(
            String xRequestID,
            String orgId) {
        return null;
    }

    @Override
    public ResponseEntity<Member> getOrgMember(
            String xRequestID,
            String orgId,
            String userId) {
        return null;
    }

    @Override
    public ResponseEntity<Role> getOrgRole(
            String xRequestID,
            String orgId,
            RoleId roleId) {
        return null;
    }

    @Override
    public ResponseEntity<InlineResponse2001> listInvitations(
            String xRequestID,
            String orgId,
            InvitationStatusName status) {
        return null;
    }

    @Override
    public ResponseEntity<InlineResponse2002> listOrgRoles(
            String xRequestID,
            String orgId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> removeMemberRole(
            String xRequestID,
            String orgId,
            String userId,
            MemberRole body) {
        return null;
    }

    @Override
    public ResponseEntity<Void> revokeInvitation(
            String xRequestID,
            String orgId,
            String invitationId,
            Body body) {
        return null;
    }
}
