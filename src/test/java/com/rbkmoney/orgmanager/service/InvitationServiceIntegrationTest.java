package com.rbkmoney.orgmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.orgmanager.OrgManagerApplication;
import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.repository.AbstractRepositoryTest;
import com.rbkmoney.orgmanager.repository.InvitationRepository;
import com.rbkmoney.orgmanager.repository.InvitationRepositoryTest;
import com.rbkmoney.orgmanager.repository.OrganizationRepository;
import com.rbkmoney.orgmanager.util.JsonMapper;
import com.rbkmoney.swag.organizations.model.InvitationListResult;
import com.rbkmoney.swag.organizations.model.InvitationStatusName;
import com.rbkmoney.swag.organizations.model.InviteeContact;
import com.rbkmoney.swag.organizations.model.ResourceScopeId;
import com.rbkmoney.swag.organizations.model.RoleId;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@DirtiesContext
@SpringBootTest(classes = OrgManagerApplication.class)
@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = InvitationRepositoryTest.Initializer.class)
public class InvitationServiceIntegrationTest extends AbstractRepositoryTest {

    private static final String INVITATION_ID = "invitationId";

    private static final String ORGANIZATION_ID = "orgId";

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    public void checkOnInvitationStatus() {
        InvitationEntity expiredInvite = buildInvitation(INVITATION_ID, LocalDateTime.now().minusDays(1));
        invitationRepository.save(expiredInvite);
        invitationService.checkOnExpiredStatus();
        expiredInvite = invitationRepository.findById(INVITATION_ID).get();
        Assert.assertEquals(InvitationStatusName.EXPIRED.getValue(), expiredInvite.getStatus());
    }

    @Test
    public void checkExpiredInviteWithPendingStatus() {
        OrganizationEntity organizationEntity = buildOrganization();
        organizationRepository.save(organizationEntity);

        InvitationEntity invite = buildInvitation(INVITATION_ID, LocalDateTime.now().plusDays(1));
        InvitationEntity expiredInvite = buildInvitation(INVITATION_ID + "_2", LocalDateTime.now().minusDays(1));
        invitationRepository.saveAll(List.of(invite, expiredInvite));

        ResponseEntity<InvitationListResult> responseEntity =
                invitationService.list(ORGANIZATION_ID, InvitationStatusName.PENDING);
        Assert.assertEquals(1, responseEntity.getBody().getResult().size());
    }

    private OrganizationEntity buildOrganization() {
        MemberEntity member = MemberEntity.builder()
                .id("memberId")
                .email("email")
                .roles(Set.of(MemberRoleEntity.builder()
                        .id(RoleId.ADMINISTRATOR.getValue())
                        .organizationId(ORGANIZATION_ID)
                        .roleId("Accountant")
                        .scopeId("Shop")
                        .resourceId("testResourceId")
                        .build()))
                .build();

        return OrganizationEntity.builder()
                .id(ORGANIZATION_ID)
                .createdAt(LocalDateTime.now())
                .name("name")
                .owner("owner")
                .members(Set.of(member))
                .build();
    }

    private InvitationEntity buildInvitation(String invitationId, LocalDateTime expiresAt) {
        return InvitationEntity.builder()
                .id(invitationId)
                .acceptToken("token")
                .createdAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .inviteeContactEmail("contactEmail")
                .inviteeContactType(InviteeContact.TypeEnum.EMAIL.getValue())
                .metadata(jsonMapper.toJson(Map.of("testKey", "testValue")))
                .organizationId(ORGANIZATION_ID)
                .status("Pending")
                .inviteeRoles(Set.of(
                        MemberRoleEntity.builder()
                                .id("role1")
                                .roleId(RoleId.ADMINISTRATOR.getValue())
                                .resourceId("resource1")
                                .scopeId(ResourceScopeId.SHOP.getValue())
                                .organizationId(ORGANIZATION_ID)
                                .build(),
                        MemberRoleEntity.builder()
                                .id("role2")
                                .roleId(RoleId.MANAGER.getValue())
                                .resourceId("resource2")
                                .scopeId(ResourceScopeId.SHOP.getValue())
                                .organizationId(ORGANIZATION_ID)
                                .build()))
                .build();
    }

}
