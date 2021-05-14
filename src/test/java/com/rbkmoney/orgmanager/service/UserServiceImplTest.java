package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.context.v1.Organization;
import com.rbkmoney.bouncer.context.v1.User;
import com.rbkmoney.orgmanager.TestObjectFactory;
import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.repository.AbstractRepositoryTest;
import com.rbkmoney.swag.organizations.model.RoleId;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class UserServiceImplTest extends AbstractRepositoryTest {

    @Autowired
    private UserService userService;

    @MockBean
    private KeycloakService keycloakService;

    @Test
    void findUserByIdWithoutOrganizations() {
        String userId = TestObjectFactory.randomString();
        AccessToken token = new AccessToken();
        token.setSubject(userId);
        token.setEmail(TestObjectFactory.randomString());
        when(keycloakService.getAccessToken()).thenReturn(token);

        User actualUser = userService.findById(userId);

        assertEquals(token.getSubject(), actualUser.getId());
        assertEquals(token.getEmail(), actualUser.getEmail());
    }

    @Test
    void findUserByIdWithOwnedOrganizations() {
        String userId = TestObjectFactory.randomString();
        AccessToken token = new AccessToken();
        token.setSubject(userId);
        token.setEmail(TestObjectFactory.randomString());
        OrganizationEntity organizationEntity = TestObjectFactory.buildOrganization();
        organizationEntity.setOwner(userId);
        organizationRepository.save(organizationEntity);
        when(keycloakService.getAccessToken()).thenReturn(token);


        User actualUser = userService.findById(userId);

        assertEquals(token.getSubject(), actualUser.getId());
        assertEquals(token.getEmail(), actualUser.getEmail());
        assertEquals(organizationEntity.getId(), actualUser.getOrgs().iterator().next().getId());
    }

    @Test
    void findMemberUserByIdWithoutOrganizations() {
        String memberId = TestObjectFactory.randomString();
        var member = TestObjectFactory.testMemberEntity(memberId);
        memberRepository.save(member);

        User actualUser = userService.findById(memberId);

        assertEquals(member.getId(), actualUser.getId());
        assertEquals(member.getEmail(), actualUser.getEmail());
        assertTrue(actualUser.getOrgs().isEmpty());
    }

    @Test
    void findMemberUserById() {
        String memberId = TestObjectFactory.randomString();
        var member = TestObjectFactory.testMemberEntity(memberId);
        OrganizationEntity organization = TestObjectFactory.buildOrganization(member);
        MemberRoleEntity memberRole = TestObjectFactory.buildMemberRole(RoleId.ACCOUNTANT, organization.getId());
        memberRoleRepository.save(memberRole);
        member.setRoles(Set.of(memberRole));
        memberRepository.save(member);
        organizationRepository.save(organization);

        User actualUser = userService.findById(memberId);

        assertEquals(member.getId(), actualUser.getId());
        assertEquals(member.getEmail(), actualUser.getEmail());
        assertEquals(organization.getId(), actualUser.getOrgs().iterator().next().getId());
        assertEquals(memberRole.getRoleId(),
                actualUser.getOrgs().iterator().next().getRoles().iterator().next().getId());
    }

    @Test
    void findMemberUserWithMemberAndOwnedOrganizationsById() {
        String memberId = TestObjectFactory.randomString();
        var member = TestObjectFactory.testMemberEntity(memberId);
        OrganizationEntity organization = TestObjectFactory.buildOrganization(member);
        MemberRoleEntity memberRole = TestObjectFactory.buildMemberRole(RoleId.ACCOUNTANT, organization.getId());
        OrganizationEntity ownedOrganization = TestObjectFactory.buildOrganization();
        ownedOrganization.setOwner(memberId);
        memberRoleRepository.save(memberRole);
        member.setRoles(Set.of(memberRole));
        memberRepository.save(member);
        organizationRepository.saveAll(List.of(organization, ownedOrganization));

        User actualUser = userService.findById(memberId);

        assertEquals(member.getId(), actualUser.getId());
        assertEquals(member.getEmail(), actualUser.getEmail());
        List<String> actualOrgs = actualUser.getOrgs().stream().map(Organization::getId).collect(Collectors.toList());
        assertTrue(actualOrgs.containsAll(List.of(organization.getId(), ownedOrganization.getId())));
        Organization actualMemberOrg = actualUser.getOrgs().stream()
                .filter(org -> org.getId().equals(organization.getId())).findFirst().get();
        assertEquals(memberRole.getRoleId(),
                actualMemberOrg.getRoles().iterator().next().getId());
    }

}