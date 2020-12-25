package com.rbkmoney.orgmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.orgmanager.OrgManagerApplication;
import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.repository.InvitationRepository;
import com.rbkmoney.orgmanager.repository.InvitationRepositoryTest;
import com.rbkmoney.orgmanager.repository.OrganizationRepository;
import com.rbkmoney.orgmanager.service.OrganizationService;
import com.rbkmoney.swag.organizations.model.InlineResponse2001;
import com.rbkmoney.swag.organizations.model.OrganizationJoinRequest;
import com.rbkmoney.swag.organizations.model.OrganizationMembership;
import com.rbkmoney.swag.organizations.model.OrganizationSearchResult;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Before
    public void setUp() throws Exception {
        keycloakOpenIdStub.givenStub();
        OrganizationEntity organizationEntity = buildOrganization();
        organizationRepository.save(organizationEntity);
        InvitationEntity invitationEntity = buildInvitation();
        invitationRepository.save(invitationEntity);
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

        ResponseEntity<InlineResponse2001> response = organizationService.listMembers(ORGANIZATION_ID);
        final boolean isMemberFounded = response.getBody().getResults()
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
        Assert.assertEquals(5, organizationSearchResultFirst.getResults().size());

        MvcResult mvcResultSecond = mockMvc.perform(get("/user/membership")
                .queryParam("limit", "5")
                .queryParam("continuationToken", organizationSearchResultFirst.getContinuationToken())
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Request-ID", "testRequestId")
        ).andExpect(status().isOk()).andReturn();

        OrganizationSearchResult organizationSearchResultSecond = objectMapper.readValue(
                mvcResultSecond.getResponse().getContentAsString(), OrganizationSearchResult.class);

        Assert.assertEquals(4, organizationSearchResultSecond.getResults().size());
        Assert.assertNull(organizationSearchResultSecond.getContinuationToken());
    }

    private InvitationEntity buildInvitation() {
        return InvitationEntity.builder()
                .id(INVITATION_ID)
                .acceptToken(ACCEPT_TOKEN)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now())
                .inviteeContactEmail("contactEmail")
                .inviteeContactType("contactType")
                .metadata("metadata")
                .organizationId(ORGANIZATION_ID)
                .status("Pending")
                .inviteeRoles(Set.of(
                        MemberRoleEntity.builder()
                                .id("role1")
                                .roleId("role1")
                                .resourceId("resource1")
                                .scopeId("scope1")
                                .organizationId(ORGANIZATION_ID)
                                .build(),
                        MemberRoleEntity.builder()
                                .id("role2")
                                .roleId("role2")
                                .resourceId("resource2")
                                .scopeId("scope2")
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
