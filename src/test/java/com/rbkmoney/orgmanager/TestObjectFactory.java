package com.rbkmoney.orgmanager;

import com.rbkmoney.bouncer.context.v1.Organization;
import com.rbkmoney.bouncer.context.v1.*;
import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.service.dto.BouncerContextDto;
import com.rbkmoney.orgmanager.service.dto.RoleDto;
import com.rbkmoney.swag.organizations.model.Invitee;
import com.rbkmoney.swag.organizations.model.*;
import org.keycloak.representations.AccessToken;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public static OrganizationEntity buildOrganization() {
        OrganizationEntity entity = new OrganizationEntity();
        entity.setId(randomString());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setName(randomString());
        entity.setOwner(randomString());
        return entity;
    }

    public static OrganizationEntity buildOrganization(MemberEntity memberEntity) {
        return OrganizationEntity.builder()
                .id(randomString())
                .createdAt(LocalDateTime.now())
                .name(randomString())
                .owner(randomString())
                .members(Set.of(memberEntity))
                .build();
    }

    public static OrganizationEntity buildOrganization(Set<MemberEntity> entities) {
        return OrganizationEntity.builder()
                .id(randomString())
                .createdAt(LocalDateTime.now())
                .name(randomString())
                .owner(randomString())
                .members(entities)
                .build();
    }

    public static Set<OrganizationEntity> buildOrganization(MemberEntity memberEntity, int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> buildOrganization(memberEntity))
                .collect(Collectors.toSet());
    }

    public static InvitationEntity buildInvitation(String orgId) {
        return InvitationEntity.builder()
                .id(randomString())
                .acceptToken(randomString())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .inviteeContactEmail("contactEmail")
                .inviteeContactType("contactType")
                .metadata("metadata")
                .organizationId(orgId)
                .status("Pending")
                .inviteeRoles(Set.of(
                        buildMemberRole(RoleId.ADMINISTRATOR, orgId),
                        buildMemberRole(RoleId.ACCOUNTANT, orgId)))
                .build();
    }

    public static MemberRoleEntity buildMemberRole(RoleId role, String orgId) {
        return MemberRoleEntity.builder()
                .id(randomString())
                .roleId(role.getValue())
                .resourceId(randomString())
                .scopeId(ResourceScopeId.SHOP.getValue())
                .organizationId(orgId)
                .active(true)
                .build();
    }

    public static String randomString() {
        return UUID.randomUUID().toString();
    }

}
