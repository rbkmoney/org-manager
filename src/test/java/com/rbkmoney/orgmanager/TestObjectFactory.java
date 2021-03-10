package com.rbkmoney.orgmanager;

import com.rbkmoney.bouncer.context.v1.Entity;
import com.rbkmoney.bouncer.context.v1.OrgRole;
import com.rbkmoney.bouncer.context.v1.OrgRoleScope;
import com.rbkmoney.bouncer.context.v1.Organization;
import com.rbkmoney.bouncer.context.v1.User;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.service.dto.BouncerContextDto;
import com.rbkmoney.orgmanager.service.dto.RoleDto;
import com.rbkmoney.swag.organizations.model.InvitationRequest;
import com.rbkmoney.swag.organizations.model.Invitee;
import com.rbkmoney.swag.organizations.model.InviteeContact;
import com.rbkmoney.swag.organizations.model.MemberRole;
import com.rbkmoney.swag.organizations.model.OrganizationJoinRequest;
import com.rbkmoney.swag.organizations.model.RoleId;
import org.keycloak.representations.AccessToken;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

public abstract class TestObjectFactory {

    public static User testUser() {
        return new User()
                .setId(randomString())
                .setEmail(randomString())
                .setOrgs(Set.of(testOrganization()));
    }

    public static Organization testOrganization() {
        return new Organization()
                .setId(randomString())
                .setOwner(new Entity().setId(randomString()))
                .setRoles(Set.of(testOrgRole()));
    }

    public static OrgRole testOrgRole() {
        return new OrgRole()
                .setId(randomString())
                .setScope(new OrgRoleScope()
                        .setShop(new Entity().setId(randomString()))
                );
    }

    public static AccessToken testToken() {
        AccessToken token = new AccessToken();
        token.exp(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .subject(randomString())
                .id(randomString());
        return token;
    }

    public static MemberEntity testMemberEntity(String id) {
        return MemberEntity.builder()
                .email(randomString())
                .id(id)
                .build();
    }

    public static BouncerContextDto testBouncerContextDto() {
        return BouncerContextDto.builder()
                .memberId(randomString())
                .operationName(randomString())
                .organizationId(randomString())
                .role(testRoleDto())
                .build();
    }

    public static RoleDto testRoleDto() {
        return RoleDto.builder()
                .roleId(randomString())
                .scopeResourceId(randomString())
                .build();
    }

    public static MemberRole testMemberRole() {
        MemberRole memberRole = new MemberRole();
        memberRole.setRoleId(RoleId.MANAGER);
        return memberRole;
    }

    public static OrganizationJoinRequest testOrganizationJoinRequest() {
        OrganizationJoinRequest organizationJoinRequest = new OrganizationJoinRequest();
        organizationJoinRequest.setInvitation(randomString());
        return organizationJoinRequest;
    }

    public static InvitationRequest testInvitationRequest() {
        InvitationRequest invitationRequest = new InvitationRequest();
        Invitee invitee = new Invitee();
        InviteeContact inviteeContact = new InviteeContact();
        inviteeContact.setEmail(randomString());
        inviteeContact.setType(InviteeContact.TypeEnum.EMAIL);
        invitee.setContact(inviteeContact);
        invitationRequest.setInvitee(invitee);
        return invitationRequest;
    }

    public static String randomString() {
        return UUID.randomUUID().toString();
    }

}
