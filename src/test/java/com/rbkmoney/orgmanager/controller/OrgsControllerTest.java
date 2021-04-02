package com.rbkmoney.orgmanager.controller;

import com.rbkmoney.orgmanager.OrgManagerApplication;
import com.rbkmoney.orgmanager.TestObjectFactory;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.exception.AccessDeniedException;
import com.rbkmoney.orgmanager.repository.InvitationRepositoryTest;
import com.rbkmoney.orgmanager.util.TestData;
import com.rbkmoney.swag.organizations.model.InvitationRequest;
import com.rbkmoney.swag.organizations.model.MemberRole;
import com.rbkmoney.swag.organizations.model.RoleId;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsAnything.anything;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {OrgManagerApplication.class, UserController.class})
@ContextConfiguration(initializers = InvitationRepositoryTest.Initializer.class)
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@TestPropertySource(locations = "classpath:wiremock.properties")
public class OrgsControllerTest extends AbstractControllerTest {

    public static final String ORGANIZATION_ID = "3Kf21K54ldE3";

    public static final String MEMBER_ID = "L6Mc2la1D9Rg";

    @Test
    void expelOrgMemberWithoutAccess() throws Exception {
        doThrow(new AccessDeniedException("Access denied")).when(resourceAccessService)
                .checkMemberRights(ORGANIZATION_ID, MEMBER_ID);

        mockMvc.perform(delete(String.format("/orgs/%s/members/%s", ORGANIZATION_ID, MEMBER_ID))
                .contentType("application/json")
                .header("Authorization", "Bearer " + generateRBKadminJwt())
                .header("X-Request-ID", "testRequestId"))
                .andExpect(status().isForbidden());
    }

    @Test
    void assignMemberRoleWithoutAccess() throws Exception {
        MemberRole memberRole = TestData.buildMemberRole();
        doThrow(new AccessDeniedException("Access denied")).when(resourceAccessService)
                .checkMemberRoleRights(ORGANIZATION_ID, MEMBER_ID, memberRole);

        mockMvc.perform(post(String.format("/orgs/%s/members/%s/roles", ORGANIZATION_ID, MEMBER_ID))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(memberRole))
                .header("Authorization", "Bearer " + generateRBKadminJwt())
                .header("X-Request-ID", "testRequestId"))
                .andExpect(status().isForbidden());
    }

    @Test
    void assignMemberRoleTest() throws Exception {
        OrganizationEntity organizationEntity = TestData.buildOrganization(ORGANIZATION_ID, MEMBER_ID);
        organizationRepository.save(organizationEntity);

        MemberEntity memberEntity = TestObjectFactory.testMemberEntity(TestObjectFactory.randomString());
        OrganizationEntity organization = TestObjectFactory.buildOrganization(memberEntity);
        MemberRoleEntity savedMemberRole = memberRoleRepository.save(TestObjectFactory.buildMemberRole(RoleId.ACCOUNTANT, organization.getId()));
        memberEntity.setRoles(Set.of(savedMemberRole));
        MemberEntity savedMember = memberRepository.save(memberEntity);
        OrganizationEntity savedOrganization = organizationRepository.save(organization);

        MemberRole memberRole = TestData.buildMemberRole();

        mockMvc.perform(post(String.format("/orgs/%s/members/%s/roles", savedOrganization.getId(), savedMember.getId()))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(memberRole))
                .header("Authorization", "Bearer " + generateRBKadminJwt())
                .header("X-Request-ID", "testRequestId"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.roleId", equalTo(memberRole.getRoleId().getValue())))
                .andExpect(jsonPath("$.scope.id", equalTo(memberRole.getScope().getId().getValue())))
                .andExpect(jsonPath("$.scope.resourceId", equalTo(memberRole.getScope().getResourceId())));

        assertFalse(memberRoleRepository.findAll().isEmpty());
    }

    @Test
    @Transactional
    void expelOrgMemberTest() throws Exception {
        MemberEntity savedMember = memberRepository.save(TestObjectFactory.testMemberEntity(TestObjectFactory.randomString()));
        OrganizationEntity savedOrganization = organizationRepository.save(TestObjectFactory.buildOrganization(savedMember));

        mockMvc.perform(delete(String.format("/orgs/%s/members/%s", savedOrganization.getId(), savedMember.getId()))
                .contentType("application/json")
                .header("Authorization", "Bearer " + generateRBKadminJwt())
                .header("X-Request-ID", "testRequestId"))
                .andExpect(status().isOk());

        Optional<OrganizationEntity> organizationEntityOptional = organizationRepository.findById(savedOrganization.getId());
        assertTrue(organizationEntityOptional.isPresent());
        assertFalse(organizationEntityOptional.get().getMembers().stream()
                .anyMatch(memberEntity -> memberEntity.getId().equals(MEMBER_ID)));
    }

    @Test
    @Transactional
    void removeMemberRoleTest() throws Exception {
        MemberEntity memberEntity = TestObjectFactory.testMemberEntity(TestObjectFactory.randomString());
        OrganizationEntity organization = TestObjectFactory.buildOrganization(memberEntity);
        MemberRoleEntity savedMemberRole = memberRoleRepository.save(TestObjectFactory.buildMemberRole(RoleId.ACCOUNTANT, organization.getId()));
        memberEntity.setRoles(Set.of(savedMemberRole));
        MemberEntity savedMember = memberRepository.save(memberEntity);
        OrganizationEntity savedOrganization = organizationRepository.save(organization);

        mockMvc.perform(delete(String.format("/orgs/%s/members/%s/roles/%s", savedOrganization.getId(), savedMember.getId(), savedMemberRole.getId()))
                .contentType("application/json")
                .header("Authorization", "Bearer " + generateRBKadminJwt())
                .header("X-Request-ID", "testRequestId")).
                andExpect(status().isNoContent());


        assertTrue(memberRoleRepository.findAll().isEmpty());
        assertThat(memberRepository.findById(savedMember.getId()).get().getRoles(), not(hasItem(savedMemberRole)));
    }

    @Test
    void createInvitationWithoutAccess() throws Exception {
        OrganizationEntity organizationEntity = TestData.buildOrganization(ORGANIZATION_ID, MEMBER_ID);
        organizationRepository.save(organizationEntity);
        InvitationRequest invitation = TestData.buildInvitationRequest();
        String body = objectMapper.writeValueAsString(invitation);

        doThrow(new AccessDeniedException("Access denied")).when(resourceAccessService)
                .checkInvitationRights(ORGANIZATION_ID, invitation);

        mockMvc.perform(post(String.format("/orgs/%s/invitations", ORGANIZATION_ID))
                .contentType("application/json")
                .content(body)
                .header("Authorization", "Bearer " + generateRBKadminJwt())
                .header("X-Request-ID", "testRequestId"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createInvitationTest() throws Exception {
        OrganizationEntity organizationEntity = TestData.buildOrganization(ORGANIZATION_ID, MEMBER_ID);
        organizationRepository.save(organizationEntity);
        InvitationRequest invitation = TestData.buildInvitationRequest();
        String body = objectMapper.writeValueAsString(invitation);

        mockMvc.perform(post(String.format("/orgs/%s/invitations", ORGANIZATION_ID))
                .contentType("application/json")
                .content(body)
                .header("Authorization", "Bearer " + generateRBKadminJwt())
                .header("X-Request-ID", "testRequestId"))
                .andExpect(jsonPath("$.status", is("Pending")));
    }

    @Test
    void listOrgMembersTest() throws Exception {
        MemberEntity savedMember = memberRepository.save(TestObjectFactory.testMemberEntity(TestObjectFactory.randomString()));
        OrganizationEntity savedOrganization = organizationRepository.save(TestObjectFactory.buildOrganization(savedMember));

        mockMvc.perform(get(String.format("/orgs/%s/members", savedOrganization.getId()))
                .contentType("application/json")
                .header("Authorization", "Bearer " + generateRBKadminJwt())
                .header("X-Request-ID", "testRequestId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", anything()));
    }

}
