package com.rbkmoney.orgmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.orgmanager.OrgManagerApplication;
import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.exception.AccessDeniedException;
import com.rbkmoney.orgmanager.exception.ResourceNotFoundException;
import com.rbkmoney.orgmanager.repository.InvitationRepository;
import com.rbkmoney.orgmanager.repository.InvitationRepositoryTest;
import com.rbkmoney.orgmanager.repository.OrganizationRepository;
import com.rbkmoney.orgmanager.service.OrganizationService;
import com.rbkmoney.orgmanager.service.ResourceAccessService;
import com.rbkmoney.swag.organizations.model.InvitationStatusName;
import com.rbkmoney.swag.organizations.model.MemberOrgListResult;
import com.rbkmoney.swag.organizations.model.OrganizationJoinRequest;
import com.rbkmoney.swag.organizations.model.OrganizationMembership;
import com.rbkmoney.swag.organizations.model.OrganizationSearchResult;
import com.rbkmoney.swag.organizations.model.ResourceScopeId;
import com.rbkmoney.swag.organizations.model.RoleId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
public class UserControllerTest extends AbstractControllerTest {

    public static String ORGANIZATION_ID = "3Kf21K54ldE3";

    public static String INVITATION_ID = "DL3Mc9dEqAlP";

    public static String MEMBER_ID = "L6Mc2la1D9Rg";

    public static String ACCEPT_TOKEN = "testToken";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private KeycloakOpenIdStub keycloakOpenIdStub;

    @SpyBean
    private OrganizationService organizationService;

    @SpyBean
    private ResourceAccessService resourceAccessService;

    @Before
    public void setUp() throws Exception {
        keycloakOpenIdStub.givenStub();
        OrganizationEntity organizationEntity = buildOrganization();
        organizationRepository.save(organizationEntity);
        InvitationEntity invitationEntity = buildInvitation();
        invitationRepository.save(invitationEntity);
    }

    @Test
    public void joinOrgTestWithResourceNotFound() throws Exception {
        OrganizationJoinRequest organizationJoinRequest = new OrganizationJoinRequest();
        organizationJoinRequest.setInvitation(ACCEPT_TOKEN);
        doThrow(new ResourceNotFoundException()).when(resourceAccessService)
                .checkOrganizationRights(organizationJoinRequest);

        mockMvc.perform(post("/user/membership")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(organizationJoinRequest))
                .header("Authorization", "Bearer " + generateRBKadminJwt())
                .header("X-Request-ID", "testRequestId"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void joinOrgTestWithoutAccess() throws Exception {
        OrganizationJoinRequest organizationJoinRequest = new OrganizationJoinRequest();
        organizationJoinRequest.setInvitation(ACCEPT_TOKEN);
        doThrow(new AccessDeniedException("Access denied")).when(resourceAccessService)
                .checkOrganizationRights(organizationJoinRequest);

        mockMvc.perform(post("/user/membership")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(organizationJoinRequest))
                .header("Authorization", "Bearer " + generateRBKadminJwt())
                .header("X-Request-ID", "testRequestId"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void joinOrgTest() throws Exception {
        OrganizationJoinRequest organizationJoinRequest = new OrganizationJoinRequest();
        organizationJoinRequest.setInvitation(ACCEPT_TOKEN);

        MvcResult mvcResult = mockMvc.perform(post("/user/membership")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(organizationJoinRequest))
                .header("Authorization", "Bearer " + generateRBKadminJwt())
                .header("X-Request-ID", "testRequestId")
        ).andExpect(status().isOk()).andReturn();

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(organizationService, atMostOnce()).joinOrganization(anyString(), argumentCaptor.capture(), anyString());
        String userId = argumentCaptor.getValue();

        OrganizationMembership organizationMembership = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(), OrganizationMembership.class);
        Assert.assertEquals(ORGANIZATION_ID, organizationMembership.getOrg().getId());
        Assert.assertEquals(userId, organizationMembership.getMember().getId());
        Assert.assertTrue(organizationMembership.getMember().getRoles().stream()
                .anyMatch(memberRole -> memberRole.getRoleId() == RoleId.ADMINISTRATOR));
        Assert.assertTrue(organizationMembership.getMember().getRoles().stream()
                .anyMatch(memberRole -> memberRole.getRoleId() == RoleId.ACCOUNTANT));

        InvitationEntity invitationEntity = invitationRepository.findById(INVITATION_ID).get();
        Assert.assertEquals(invitationEntity.getStatus(), InvitationStatusName.ACCEPTED.getValue());
    }

    @Test
    public void cancelOrgMembershipTest() throws Exception {
        String jwtToken = generateRBKadminJwt();

        OrganizationJoinRequest organizationJoinRequest = new OrganizationJoinRequest();
        organizationJoinRequest.setInvitation(ACCEPT_TOKEN);
        mockMvc.perform(post("/user/membership")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(organizationJoinRequest))
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Request-ID", "testRequestId")
        ).andExpect(status().isOk()).andReturn();

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(organizationService, atMostOnce()).joinOrganization(anyString(), argumentCaptor.capture(), anyString());
        String userId = argumentCaptor.getValue();

        MvcResult mvcResult = mockMvc.perform(delete("/user/membership/{orgId}", ORGANIZATION_ID)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Request-ID", "testRequestId")
        ).andExpect(status().isOk()).andReturn();

        ResponseEntity<MemberOrgListResult> response = organizationService.listMembers(ORGANIZATION_ID);
        final boolean isMemberFounded = response.getBody().getResult()
                .stream().anyMatch(member -> member.getId().equals(userId));
        Assert.assertFalse(isMemberFounded);
    }

    @Test
    public void inquireOrgMembershipTest() throws Exception {
        String jwtToken = generateRBKadminJwt();

        // Join organization
        OrganizationJoinRequest organizationJoinRequest = new OrganizationJoinRequest();
        organizationJoinRequest.setInvitation(ACCEPT_TOKEN);

        MvcResult mvcResult = mockMvc.perform(post("/user/membership")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(organizationJoinRequest))
                .header("Authorization", "Bearer " + generateRBKadminJwt())
                .header("X-Request-ID", "testRequestId")
        ).andExpect(status().isOk()).andReturn();

        // get membership
        mockMvc.perform(get("/user/membership/{orgId}", ORGANIZATION_ID)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Request-ID", "testRequestId")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.org").exists())
                .andExpect(jsonPath("$.member").exists());
    }

    @Test
    public void listOrgMembershipTest() throws Exception {
        String jwtToken = generateRBKadminJwt();
        for (int i = 0; i < 8; i++) {
            OrganizationEntity organizationEntity = buildOrganization();
            organizationEntity.setId(UUID.randomUUID().toString());
            Set<MemberEntity> memberEntities = organizationEntity.getMembers().stream()
                    .peek(memberEntity -> memberEntity.setId(UUID.randomUUID().toString()))
                    .collect(Collectors.toSet());
            organizationEntity.setMembers(memberEntities);
            organizationRepository.save(organizationEntity);
        }

        MvcResult mvcResultFirst = mockMvc.perform(get("/user/membership")
                .queryParam("limit", "5")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Request-ID", "testRequestId")
        ).andExpect(status().isOk()).andReturn();

        OrganizationSearchResult organizationSearchResultFirst = objectMapper.readValue(
                mvcResultFirst.getResponse().getContentAsString(), OrganizationSearchResult.class);
        Assert.assertEquals(5, organizationSearchResultFirst.getResult().size());

        MvcResult mvcResultSecond = mockMvc.perform(get("/user/membership")
                .queryParam("limit", "5")
                .queryParam("continuationToken", organizationSearchResultFirst.getContinuationToken())
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Request-ID", "testRequestId")
        ).andExpect(status().isOk()).andReturn();

        OrganizationSearchResult organizationSearchResultSecond = objectMapper.readValue(
                mvcResultSecond.getResponse().getContentAsString(), OrganizationSearchResult.class);

        Assert.assertEquals(4, organizationSearchResultSecond.getResult().size());
        Assert.assertNull(organizationSearchResultSecond.getContinuationToken());
    }

    private InvitationEntity buildInvitation() {
        return InvitationEntity.builder()
                .id(INVITATION_ID)
                .acceptToken(ACCEPT_TOKEN)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .inviteeContactEmail("contactEmail")
                .inviteeContactType("contactType")
                .metadata("metadata")
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
                                .roleId(RoleId.ACCOUNTANT.getValue())
                                .resourceId("resource2")
                                .scopeId(ResourceScopeId.SHOP.getValue())
                                .organizationId(ORGANIZATION_ID)
                                .build()))
                .build();
    }

    private OrganizationEntity buildOrganization() {
        MemberEntity member = MemberEntity.builder()
                .id(MEMBER_ID)
                .email("email")
                .build();

        return OrganizationEntity.builder()
                .id(ORGANIZATION_ID)
                .createdAt(LocalDateTime.now())
                .name("name")
                .owner("owner")
                .members(Set.of(member))
                .build();
    }

}
