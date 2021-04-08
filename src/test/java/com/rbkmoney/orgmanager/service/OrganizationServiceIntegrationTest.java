package com.rbkmoney.orgmanager.service;


import com.rbkmoney.orgmanager.TestObjectFactory;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.repository.AbstractRepositoryTest;
import com.rbkmoney.swag.organizations.model.Member;
import com.rbkmoney.swag.organizations.model.MemberRole;
import com.rbkmoney.swag.organizations.model.RoleId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrganizationServiceIntegrationTest extends AbstractRepositoryTest {

    @Autowired
    private OrganizationService organizationService;

    @Test
    @Transactional
    void shouldGetOrgMember() {

        // Given
        MemberEntity member = TestObjectFactory.testMemberEntity(TestObjectFactory.randomString());
        OrganizationEntity organization = TestObjectFactory.buildOrganization(member);
        MemberRoleEntity savedMemberRoleInOrg =
                memberRoleRepository.save(TestObjectFactory.buildMemberRole(RoleId.ACCOUNTANT, organization.getId()));
        MemberRoleEntity savedMemberRoleInAnotherOrg =
                memberRoleRepository
                        .save(TestObjectFactory.buildMemberRole(RoleId.ACCOUNTANT, TestObjectFactory.randomString()));
        member.setRoles(Set.of(savedMemberRoleInOrg, savedMemberRoleInAnotherOrg));
        MemberEntity savedMember = memberRepository.save(member);
        OrganizationEntity savedOrganization = organizationRepository.save(organization);

        // When
        Member actualMember = organizationService.getOrgMember(savedMember.getId(), savedOrganization.getId());

        // Then
        assertEquals(savedMember.getId(), actualMember.getId());
        assertEquals(savedMember.getEmail(), actualMember.getUserEmail());
        List<String> roles = actualMember.getRoles().stream().map(MemberRole::getId).collect(toList());
        assertThat(roles, hasItem(savedMemberRoleInOrg.getId()));
        assertThat(roles, not(hasItem(savedMemberRoleInAnotherOrg.getId())));
        assertEquals(2, memberRepository.findById(savedMember.getId()).get().getRoles().size());
    }

}
