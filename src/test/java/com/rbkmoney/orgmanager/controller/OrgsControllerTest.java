package com.rbkmoney.orgmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.orgmanager.OrgManagerApplication;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.repository.InvitationRepositoryTest;
import com.rbkmoney.orgmanager.repository.MemberRepository;
import com.rbkmoney.orgmanager.repository.OrganizationRepository;
import com.rbkmoney.orgmanager.service.OrganizationService;
import com.rbkmoney.orgmanager.util.TestData;
import com.rbkmoney.swag.organizations.model.InvitationRequest;
import com.rbkmoney.swag.organizations.model.Invitee;
import com.rbkmoney.swag.organizations.model.InviteeContact;
import com.rbkmoney.swag.organizations.model.MemberRole;
import com.rbkmoney.swag.organizations.model.MemberRoleScope;
import com.rbkmoney.swag.organizations.model.OrganizationMembership;
import com.rbkmoney.swag.organizations.model.ResourceScopeId;
import com.rbkmoney.swag.organizations.model.RoleId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsAnything.anything;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {OrgManagerApplication.class, UserController.class})
@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = InvitationRepositoryTest.Initializer.class)
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@TestPropertySource(locations = "classpath:wiremock.properties")
public class OrgsControllerTest extends AbstractControllerTest {

    public static final String ORGANIZATION_ID = "3Kf21K54ldE3";

    public static final String MEMBER_ID = "L6Mc2la1D9Rg";

    public static final String MEMBER_ROLE_ID = "D3KwP29McT";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private KeycloakOpenIdStub keycloakOpenIdStub;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private OrganizationService organizationService;

    @Before
    public void setUp() throws Exception {
        keycloakOpenIdStub.givenStub();
        OrganizationEntity organizationEntity = TestData.buildOrganization(ORGANIZATION_ID, MEMBER_ID);
        organizationRepository.save(organizationEntity);
    }

    @Test
    public void assignMemberRoleTest() throws Exception {
        MemberRole memberRole = TestData.buildMemberRole();

        mockMvc.perform(put(String.format("/orgs/%s/members/%s/roles", ORGANIZATION_ID, MEMBER_ID))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(memberRole))
                .header("Authorization", "Bearer " + generateRBKadminJwt())
                .header("X-Request-ID", "testRequestId")
        ).andExpect(status().isOk());

        OrganizationMembership organizationMembership =
                organizationService.getMembership(ORGANIZATION_ID, MEMBER_ID, "email").getBody();
        Assert.assertTrue(organizationMembership.getMember().getRoles()
                .stream().anyMatch(role -> role.getRoleId() == RoleId.ADMINISTRATOR));
        Optional<MemberRole> memberRoleOptional = organizationMembership.getMember().getRoles().stream()
                .filter(role -> role.getRoleId() == RoleId.ADMINISTRATOR)
                .findFirst();
        Assert.assertTrue(memberRoleOptional.isPresent());
        Assert.assertEquals(memberRole.getScope().getId(), memberRoleOptional.get().getScope().getId());
        Assert.assertEquals(memberRole.getScope().getResourceId(), memberRoleOptional.get().getScope().getResourceId());
    }

    @Test
    public void expelOrgMemberTest() throws Exception {
        mockMvc.perform(delete(String.format("/orgs/%s/members/%s", ORGANIZATION_ID, MEMBER_ID))
                .contentType("application/json")
                .header("Authorization", "Bearer " + generateRBKadminJwt())
                .header("X-Request-ID", "testRequestId")
        ).andExpect(status().isOk());

        Optional<OrganizationEntity> organizationEntityOptional = organizationService.findById(ORGANIZATION_ID);
        Assert.assertTrue(organizationEntityOptional.isPresent());
        Assert.assertFalse(organizationEntityOptional.get().getMembers().stream()
                .anyMatch(memberEntity -> memberEntity.getId().equals(MEMBER_ID)));
    }

    @Test
    public void removeMemberRoleTest() throws Exception {
        MemberRole memberRole = TestData.buildMemberRole();
        mockMvc.perform(delete(String.format("/orgs/%s/members/%s/roles", ORGANIZATION_ID, MEMBER_ID))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(memberRole))
                .header("Authorization", "Bearer " + generateRBKadminJwt())
                .header("X-Request-ID", "testRequestId")
        ).andExpect(status().isOk());

        Optional<OrganizationEntity> organizationEntityOptional = organizationService.findById(ORGANIZATION_ID);
        Assert.assertTrue(organizationEntityOptional.isPresent());
        Optional<MemberEntity> memberEntityOptional = organizationEntityOptional.get().getMembers().stream()
                .filter(memberEntity -> memberEntity.getId().equals(MEMBER_ID))
                .findFirst();
        Assert.assertTrue(memberEntityOptional.isPresent());
        Assert.assertFalse(memberEntityOptional.get().getRoles().stream()
                .anyMatch(memberRoleEntity -> memberRoleEntity.getId().equals(MEMBER_ID)));
    }

    @Test
    public void createInvitationTest() throws Exception {
        InvitationRequest invitation = TestData.buildInvitationRequest();
        String body = objectMapper.writeValueAsString(invitation);

        mockMvc.perform(post(String.format("/orgs/%s/invitations", ORGANIZATION_ID))
                .contentType("application/json")
                .content(body)
                .header("Authorization", "Bearer " + generateRBKadminJwt())
                .header("X-Request-ID", "testRequestId")
        ).andExpect(jsonPath("$.status", is("Pending")));
    }

    @Test
    public void listOrgMembersTest() throws Exception {
        mockMvc.perform(get(String.format("/orgs/%s/members", ORGANIZATION_ID))
                .contentType("application/json")
                .header("Authorization", "Bearer " + generateRBKadminJwt())
                .header("X-Request-ID", "testRequestId")
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.result", anything()));
    }

}
