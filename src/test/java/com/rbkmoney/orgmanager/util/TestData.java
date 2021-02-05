package com.rbkmoney.orgmanager.util;

import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.swag.organizations.model.InvitationRequest;
import com.rbkmoney.swag.organizations.model.Invitee;
import com.rbkmoney.swag.organizations.model.InviteeContact;
import com.rbkmoney.swag.organizations.model.MemberRole;
import com.rbkmoney.swag.organizations.model.MemberRoleScope;
import com.rbkmoney.swag.organizations.model.ResourceScopeId;
import com.rbkmoney.swag.organizations.model.RoleId;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestData {

    public static OrganizationEntity buildOrganization(String organizationId, String memberId) {
        MemberEntity member = MemberEntity.builder()
                .id(memberId)
                .email("email")
                .roles(Set.of(MemberRoleEntity.builder()
                        .id(RoleId.ADMINISTRATOR.getValue())
                        .organizationId(organizationId)
                        .roleId("Accountant")
                        .scopeId("Shop")
                        .resourceId("testResourceId")
                        .build()))
                .build();

        return OrganizationEntity.builder()
                .id(organizationId)
                .createdAt(LocalDateTime.now())
                .name("name")
                .owner("owner")
                .members(Set.of(member))
                .build();
    }

    public static InvitationEntity buildInvitation(
            String organizationId,
            String invitationId,
            LocalDateTime expiresAt
    ) {
        return InvitationEntity.builder()
                .id(invitationId)
                .acceptToken("token")
                .createdAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .inviteeContactEmail("contactEmail")
                .inviteeContactType(InviteeContact.TypeEnum.EMAIL.getValue())
                .organizationId(organizationId)
                .status("Pending")
                .inviteeRoles(Set.of(
                        MemberRoleEntity.builder()
                                .id("role1")
                                .roleId(RoleId.ADMINISTRATOR.getValue())
                                .resourceId("resource1")
                                .scopeId(ResourceScopeId.SHOP.getValue())
                                .organizationId(organizationId)
                                .build(),
                        MemberRoleEntity.builder()
                                .id("role2")
                                .roleId(RoleId.MANAGER.getValue())
                                .resourceId("resource2")
                                .scopeId(ResourceScopeId.SHOP.getValue())
                                .organizationId(organizationId)
                                .build()))
                .build();
    }

    public static InvitationRequest buildInvitationRequest() {
        InvitationRequest invitation = new InvitationRequest();

        Invitee invitee = new Invitee();

        InviteeContact inviteeContact = new InviteeContact();
        inviteeContact.setEmail("testEmail@mail.ru");
        inviteeContact.setType(InviteeContact.TypeEnum.EMAIL);
        invitee.setContact(inviteeContact);

        invitee.setRoles(Set.of(buildMemberRole()));

        invitation.setInvitee(invitee);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("testKey", "testValue");
        invitation.setMetadata(metadata);

        return invitation;
    }

    public static MemberRole buildMemberRole() {
        MemberRole memberRole = new MemberRole();
        memberRole.setRoleId(RoleId.ADMINISTRATOR);
        MemberRoleScope memberRoleScope = new MemberRoleScope();
        memberRoleScope.setId(ResourceScopeId.SHOP);
        memberRoleScope.setResourceId("testResourceIdKek");
        memberRole.setScope(memberRoleScope);

        return memberRole;
    }

}
