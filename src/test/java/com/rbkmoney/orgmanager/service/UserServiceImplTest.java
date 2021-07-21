package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.TestObjectFactory;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.repository.AbstractRepositoryTest;
import com.rbkmoney.orgmanager.service.model.UserInfo;
import com.rbkmoney.swag.organizations.model.RoleId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceImplTest extends AbstractRepositoryTest {

    @Autowired
    private UserService userService;

    @Test
    void dontFindUserWithoutOrganizations() {
        String userId = TestObjectFactory.randomString();

        UserInfo userInfo = userService.findById(userId);

        assertNull(userInfo.getMember());
        assertEquals(Set.of(), userInfo.getOrganizations());
    }

    @Test
    void findUserWithOwnedOrganizations() {
        String userId = TestObjectFactory.randomString();
        OrganizationEntity organizationEntity = TestObjectFactory.buildOrganization();
        organizationEntity.setOwner(userId);
        organizationRepository.save(organizationEntity);

        UserInfo userInfo = userService.findById(userId);

        assertNull(userInfo.getMember());
        assertEquals(organizationEntity.getId(), userInfo.getOrganizations().iterator().next().getId());
    }

    @Test
    void findMemberUserWithoutOrganizations() {
        String memberId = TestObjectFactory.randomString();
        var member = TestObjectFactory.testMemberEntity(memberId);
        memberRepository.save(member);

        UserInfo userInfo = userService.findById(memberId);
        MemberEntity user = userInfo.getMember();

        assertEquals(member.getId(), user.getId());
        assertEquals(member.getEmail(), user.getEmail());
        assertTrue(userInfo.getOrganizations().isEmpty());
    }

    @Test
    void findMemberUserWithOrganizations() {
        String memberId = TestObjectFactory.randomString();
        var member = TestObjectFactory.testMemberEntity(memberId);
        OrganizationEntity organization = TestObjectFactory.buildOrganization(member);
        MemberRoleEntity memberRole = TestObjectFactory.buildMemberRole(RoleId.ACCOUNTANT, organization.getId());
        memberRoleRepository.save(memberRole);
        member.setRoles(Set.of(memberRole));
        memberRepository.save(member);
        organizationRepository.save(organization);

        UserInfo userInfo = userService.findById(memberId);
        MemberEntity user = userInfo.getMember();

        assertEquals(member.getId(), user.getId());
        assertEquals(member.getEmail(), user.getEmail());
        assertEquals(organization.getId(), userInfo.getOrganizations().iterator().next().getId());
    }

    @Test
    void findMemberUserWithMemberAndOwnedOrganizations() {
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

        UserInfo userInfo = userService.findById(memberId);
        MemberEntity user = userInfo.getMember();

        assertEquals(member.getId(), user.getId());
        assertEquals(member.getEmail(), user.getEmail());
        List<String> actualOrgs = userInfo.getOrganizations()
                .stream()
                .map(OrganizationEntity::getId)
                .collect(Collectors.toList());
        assertTrue(actualOrgs.containsAll(List.of(organization.getId(), ownedOrganization.getId())));
    }

    @Test
    void findMemberUserWithSameMemberAndOwnedOrganizations() {
        String memberId = TestObjectFactory.randomString();
        var member = TestObjectFactory.testMemberEntity(memberId);
        OrganizationEntity organization = TestObjectFactory.buildOrganization(member);
        organization.setOwner(memberId);
        organization.setRoles(
                Set.of(TestObjectFactory.buildOrganizationRole(RoleId.ACCOUNTANT, organization.getId()))
        );
        MemberRoleEntity memberRole = TestObjectFactory.buildMemberRole(RoleId.ACCOUNTANT, organization.getId());
        memberRoleRepository.save(memberRole);
        member.setRoles(Set.of(memberRole));
        memberRepository.save(member);
        organizationRepository.save(organization);

        UserInfo userInfo = userService.findById(memberId);
        MemberEntity user = userInfo.getMember();

        assertEquals(member.getId(), user.getId());
        assertEquals(member.getEmail(), user.getEmail());
        assertEquals(1, userInfo.getOrganizations().size());
        assertEquals(organization.getId(), userInfo.getOrganizations().iterator().next().getId());
    }
}
