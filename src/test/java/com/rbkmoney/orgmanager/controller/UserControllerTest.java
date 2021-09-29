package com.rbkmoney.orgmanager.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rbkmoney.orgmanager.entity.*;
import com.rbkmoney.orgmanager.exception.AccessDeniedException;
import com.rbkmoney.orgmanager.exception.ResourceNotFoundException;
import com.rbkmoney.orgmanager.service.dto.ResourceDto;
import com.rbkmoney.swag.organizations.model.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.rbkmoney.orgmanager.TestObjectFactory.*;
import static com.rbkmoney.orgmanager.controller.JwtTokenBuilder.DEFAULT_EMAIL;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerTest extends AbstractControllerTest {

    @Test
    void joinOrgTestWithResourceNotFound() throws Exception {
        OrganizationJoinRequest organizationJoinRequest = new OrganizationJoinRequest();
        organizationJoinRequest.setInvitation(randomString());
        doThrow(new ResourceNotFoundException()).when(resourceAccessService)
                .checkRights(ArgumentMatchers.any(ResourceDto.class));

        mockMvc.perform(post("/user/membership")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(organizationJoinRequest))
                .header("Authorization", "Bearer " + generateRbkAdminJwt())
                .header("X-Request-ID", "testRequestId"))
                .andExpect(status().isNotFound());
    }

    @Test
    void joinOrgTestWithoutAccess() throws Exception {
        OrganizationJoinRequest organizationJoinRequest = new OrganizationJoinRequest();
        organizationJoinRequest.setInvitation(randomString());
        doThrow(new AccessDeniedException("Access denied")).when(resourceAccessService)
                .checkRights(ArgumentMatchers.any(ResourceDto.class));

        mockMvc.perform(post("/user/membership")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(organizationJoinRequest))
                .header("Authorization", "Bearer " + generateRbkAdminJwt())
                .header("X-Request-ID", "testRequestId"))
                .andExpect(status().isForbidden());
    }

    @Test
    void joinOrgTestWrongUserEmail() throws Exception {
        String jwtToken = generateRbkAdminJwt();
        OrganizationEntity savedOrg = organizationRepository.save(buildOrganization());
        InvitationEntity savedInvitation = invitationRepository.save(buildInvitation(savedOrg.getId()));
        OrganizationJoinRequest organizationJoinRequest = new OrganizationJoinRequest();
        organizationJoinRequest.setInvitation(savedInvitation.getAcceptToken());

        mockMvc.perform(post("/user/membership")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(organizationJoinRequest))
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Request-ID", "testRequestId"))
                .andExpect(status().isForbidden());
    }

    @Test
    void joinOrgTestInviteAlreadyAccepted() throws Exception {
        OrganizationEntity savedOrg = organizationRepository.save(buildOrganization());
        InvitationEntity acceptedInvitationEntity = buildInvitation(savedOrg.getId(), DEFAULT_EMAIL);
        acceptedInvitationEntity.setAcceptedAt(LocalDateTime.now());
        acceptedInvitationEntity.setAcceptedMemberId(randomString());
        acceptedInvitationEntity.setStatus(InvitationStatusName.ACCEPTED.getValue());
        InvitationEntity savedInvitation = invitationRepository.save(acceptedInvitationEntity);
        OrganizationJoinRequest organizationJoinRequest = new OrganizationJoinRequest();
        organizationJoinRequest.setInvitation(savedInvitation.getAcceptToken());

        mockMvc.perform(post("/user/membership")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(organizationJoinRequest))
                .header("Authorization", "Bearer " + generateRbkAdminJwt())
                .header("X-Request-ID", "testRequestId"))
                .andExpect(status().is(422));
    }

    @Test
    void joinOrgNewMemberTest() throws Exception {
        String jwtToken = generateRbkAdminJwt();
        String userId = getUserFromToken();
        OrganizationEntity savedOrg = organizationRepository.save(buildOrganization());
        InvitationEntity savedInvitation = invitationRepository.save(buildInvitation(savedOrg.getId(), DEFAULT_EMAIL));
        OrganizationJoinRequest organizationJoinRequest = new OrganizationJoinRequest();
        organizationJoinRequest.setInvitation(savedInvitation.getAcceptToken());

        MvcResult mvcResult = mockMvc.perform(post("/user/membership")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(organizationJoinRequest))
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Request-ID", "testRequestId"))
                .andExpect(status().isOk())
                .andReturn();

        OrganizationMembership organizationMembership = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(), OrganizationMembership.class);
        assertEquals(savedOrg.getId(), organizationMembership.getOrg().getId());
        assertEquals(userId, organizationMembership.getMember().getId());
        List<String> actualRoles =
                organizationMembership.getMember().getRoles().stream().map(MemberRole::getRoleId).map(RoleId::getValue)
                        .collect(Collectors.toList());
        List<String> expectedRoles = savedInvitation.getInviteeRoles().stream().map(MemberRoleEntity::getRoleId)
                .collect(Collectors.toList());
        assertIterableEquals(expectedRoles, actualRoles);
        InvitationEntity invitationEntity = invitationRepository.findById(savedInvitation.getId()).get();
        assertEquals(invitationEntity.getStatus(), InvitationStatusName.ACCEPTED.getValue());
    }

    @Test
    void joinOrgExistMemberTest() throws Exception {
        String jwtToken = generateRbkAdminJwt();
        String userId = getUserFromToken();
        memberRepository.save(testMemberEntity(userId));
        OrganizationEntity savedOrg = organizationRepository.save(buildOrganization());
        InvitationEntity savedInvitation = invitationRepository.save(buildInvitation(savedOrg.getId(), DEFAULT_EMAIL));
        OrganizationJoinRequest organizationJoinRequest = new OrganizationJoinRequest();
        organizationJoinRequest.setInvitation(savedInvitation.getAcceptToken());

        MvcResult mvcResult = mockMvc.perform(post("/user/membership")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(organizationJoinRequest))
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Request-ID", "testRequestId"))
                .andExpect(status().isOk())
                .andReturn();

        OrganizationMembership organizationMembership = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(), OrganizationMembership.class);
        assertEquals(savedOrg.getId(), organizationMembership.getOrg().getId());
        assertEquals(userId, organizationMembership.getMember().getId());
        List<String> actualRoles =
                organizationMembership.getMember().getRoles().stream().map(MemberRole::getRoleId).map(RoleId::getValue)
                        .collect(Collectors.toList());
        List<String> expectedRoles = savedInvitation.getInviteeRoles().stream().map(MemberRoleEntity::getRoleId)
                .collect(Collectors.toList());
        assertIterableEquals(expectedRoles, actualRoles);

        InvitationEntity invitationEntity = invitationRepository.findById(savedInvitation.getId()).get();
        assertEquals(invitationEntity.getStatus(), InvitationStatusName.ACCEPTED.getValue());
    }

    @Test
    @Transactional
    void cancelOrgMembershipTest() throws Exception {
        String jwtToken = generateRbkAdminJwt();
        String userId = getUserFromToken();
        MemberEntity member = memberRepository.save(testMemberEntity(userId));
        OrganizationEntity orgWithMember = organizationRepository.save(buildOrganization(member));

        mockMvc.perform(delete("/user/membership/{orgId}", orgWithMember.getId())
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Request-ID", "testRequestId"))
                .andExpect(status().isOk());

        OrganizationEntity organizationEntity = organizationRepository.findById(orgWithMember.getId()).get();
        assertFalse(organizationEntity.getMembers().contains(member));
    }

    @Test
    void inquireOrgMembershipTest() throws Exception {
        String jwtToken = generateRbkAdminJwt();
        String userId = getUserFromToken();
        MemberEntity member = memberRepository.save(testMemberEntity(userId));
        OrganizationEntity orgWithMember = organizationRepository.save(buildOrganization(member));

        mockMvc.perform(get("/user/membership/{orgId}", orgWithMember.getId())
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Request-ID", "testRequestId")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.org").exists())
                .andExpect(jsonPath("$.org.id", equalTo(orgWithMember.getId())))
                .andExpect(jsonPath("$.member").exists())
                .andExpect(jsonPath("$.member.id", equalTo(userId)));
    }

    /**
     * JD - 395
     */
    @Test
    @Transactional
    void listOrgMembershipAfterCancel() throws Exception {
        String jwtToken = generateRbkAdminJwt();
        String userId = getUserFromToken();
        MemberEntity member = memberRepository.save(testMemberEntity(userId));
        OrganizationEntity orgWithMember = organizationRepository.save(buildOrganization(member));

        MvcResult mvcResult = mockMvc.perform(get("/user/membership")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Request-ID", "testRequestId"))
                .andExpect(status().isOk())
                .andReturn();

        OrganizationSearchResult searchResult =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), OrganizationSearchResult.class);
        assertEquals(1, searchResult.getResult().size());
        assertEquals(orgWithMember.getId(), searchResult.getResult().get(0).getId());

        mockMvc.perform(delete("/user/membership/{orgId}", orgWithMember.getId())
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Request-ID", "testRequestId"))
                .andExpect(status().isOk());


        mvcResult = mockMvc.perform(get("/user/membership")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Request-ID", "testRequestId"))
                .andExpect(status().isOk())
                .andReturn();

        searchResult =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), OrganizationSearchResult.class);
        assertTrue(searchResult.getResult().isEmpty());
    }

    @Test
    void listOrgMembershipWithoutLimitTest() throws Exception {
        String jwtToken = generateRbkAdminJwt();
        String userId = getUserFromToken();
        MemberEntity targetMember = memberRepository.save(testMemberEntity(userId));
        Set<OrganizationEntity> targetEntities = buildOrganization(targetMember, 7);
        organizationRepository.saveAll(targetEntities);

        MvcResult mvcResultFirst = mockMvc.perform(get("/user/membership")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Request-ID", "testRequestId"))
                .andExpect(status().isOk())
                .andReturn();

        OrganizationSearchResult organizationSearchResult = objectMapper.readValue(
                mvcResultFirst.getResponse().getContentAsString(), OrganizationSearchResult.class);
        assertEquals(7, organizationSearchResult.getResult().size());
    }

    @Test
    void listOrgMembershipTest() throws Exception {
        String userId = getUserFromToken();
        MemberEntity targetMember = memberRepository.save(testMemberEntity(userId));
        Set<OrganizationEntity> targetEntities = buildOrganization(targetMember, 9);
        OrganizationEntity anotherOrganization = buildOrganization();
        OrganizationEntity organizationWithOwner = buildOrganization();
        organizationWithOwner.setOwner(userId);
        targetEntities.addAll(List.of(anotherOrganization, organizationWithOwner));
        organizationRepository.saveAll(targetEntities);
        String limit = "4";
        String jwtToken = generateRbkAdminJwt();

        MvcResult mvcResultFirst = mockMvc.perform(get("/user/membership")
                .queryParam("limit", limit)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Request-ID", "testRequestId"))
                .andExpect(status().isOk())
                .andReturn();

        OrganizationSearchResult organizationSearchResultFirst = objectMapper.readValue(
                mvcResultFirst.getResponse().getContentAsString(), OrganizationSearchResult.class);
        assertEquals(4, organizationSearchResultFirst.getResult().size());

        MvcResult mvcResultSecond = mockMvc.perform(get("/user/membership")
                .queryParam("limit", limit)
                .queryParam("continuationToken", organizationSearchResultFirst.getContinuationToken())
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Request-ID", "testRequestId"))
                .andExpect(status().isOk())
                .andReturn();

        OrganizationSearchResult organizationSearchResultSecond = objectMapper.readValue(
                mvcResultSecond.getResponse().getContentAsString(), OrganizationSearchResult.class);
        assertEquals(4, organizationSearchResultSecond.getResult().size());

        MvcResult mvcResultThird = mockMvc.perform(get("/user/membership")
                .queryParam("limit", limit)
                .queryParam("continuationToken", organizationSearchResultSecond.getContinuationToken())
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Request-ID", "testRequestId"))
                .andExpect(status().isOk())
                .andReturn();

        OrganizationSearchResult organizationSearchResultThird = objectMapper.readValue(
                mvcResultThird.getResponse().getContentAsString(), OrganizationSearchResult.class);

        assertEquals(2, organizationSearchResultThird.getResult().size());
        assertNull(organizationSearchResultThird.getContinuationToken());
    }

    @Test
    void switchOrganizationWithNewContextCreation() throws Exception {
        String userId = getUserFromToken();
        MemberEntity memberEntity = memberRepository.save(testMemberEntity(userId));
        OrganizationEntity organizationEntity = organizationRepository.save(buildOrganization());
        String jwtToken = generateRbkAdminJwt();
        OrganizationSwitchRequest organizationSwitchRequest = new OrganizationSwitchRequest();
        organizationSwitchRequest.setOrganizationId(organizationEntity.getId());

        mockMvc.perform(put("/user/context")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + jwtToken)
                        .header("X-Request-ID", "testRequestId")
                        .content(objectMapper.writeValueAsString(organizationSwitchRequest))
                )
                .andExpect(status().isNoContent());
        Optional<MemberContextEntity> memberContextEntityOptional = memberContextRepository.findByMemberEntityId(userId);

        assertTrue(memberContextEntityOptional.isPresent());
        assertEquals(userId, memberContextEntityOptional.get().getMemberEntity().getId());
        assertEquals(organizationEntity.getId(), memberContextEntityOptional.get().getOrganizationEntity().getId());
    }

    @Test
    void switchOrganizationWithExistsContext() throws Exception {
        String userId = getUserFromToken();
        MemberEntity memberEntity = memberRepository.save(testMemberEntity(userId));
        OrganizationEntity organizationEntity = organizationRepository.save(buildOrganization());
        MemberContextEntity memberContextEntity = memberContextRepository.save(
                MemberContextEntity.builder()
                        .memberEntity(memberEntity)
                        .organizationEntity(organizationEntity)
                        .build()
        );
        OrganizationEntity newOrganizationEntity = organizationRepository.save(buildOrganization());
        String jwtToken = generateRbkAdminJwt();
        OrganizationSwitchRequest organizationSwitchRequest = new OrganizationSwitchRequest();
        organizationSwitchRequest.setOrganizationId(newOrganizationEntity.getId());

        mockMvc.perform(put("/user/context")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + jwtToken)
                        .header("X-Request-ID", "testRequestId")
                        .content(objectMapper.writeValueAsString(organizationSwitchRequest))
                )
                .andExpect(status().isNoContent());
        Optional<MemberContextEntity> memberContextEntityOptional = memberContextRepository.findByMemberEntityId(userId);

        assertTrue(memberContextEntityOptional.isPresent());
        assertEquals(newOrganizationEntity.getId(), memberContextEntityOptional.get().getOrganizationEntity().getId());
    }

    @Test
    void getMemberContext() throws Exception {
        String userId = getUserFromToken();
        MemberEntity memberEntity = memberRepository.save(testMemberEntity(userId));
        OrganizationEntity organizationEntity = organizationRepository.save(buildOrganization());
        MemberContextEntity memberContextEntity = memberContextRepository.save(
                MemberContextEntity.builder()
                        .memberEntity(memberEntity)
                        .organizationEntity(organizationEntity)
                        .build()
        );
        String jwtToken = generateRbkAdminJwt();

        MvcResult mvcResult = mockMvc.perform(get("/user/context")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .header("X-Request-ID", "testRequestId")
                )
                .andExpect(status().isOk())
                .andReturn();

        System.out.println(mvcResult);
    }

}
