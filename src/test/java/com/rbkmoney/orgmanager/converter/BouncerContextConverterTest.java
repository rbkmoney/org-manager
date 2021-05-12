package com.rbkmoney.orgmanager.converter;

import com.rbkmoney.bouncer.context.v1.Entity;
import com.rbkmoney.bouncer.context.v1.OrgRole;
import com.rbkmoney.bouncer.context.v1.OrgRoleScope;
import com.rbkmoney.bouncer.context.v1.User;
import com.rbkmoney.orgmanager.TestObjectFactory;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.swag.organizations.model.RoleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class BouncerContextConverterTest {

    private BouncerContextConverter converter;

    @BeforeEach
    void setUp() {
        converter = new BouncerContextConverter();
    }

    @Test
    void shouldConvertToOrgRole() {
        MemberRoleEntity entity = MemberRoleEntity.builder()
                .id("id")
                .roleId("Administrator")
                .scopeId("Shop")
                .resourceId("resource")
                .organizationId("org")
                .build();

        OrgRole role = converter.toOrgRole(entity);

        OrgRole expected = new OrgRole()
                .setId(RoleId.ADMINISTRATOR.getValue())
                .setScope(new OrgRoleScope()
                        .setShop(new Entity().setId("resource")));

        assertThat(role).isEqualToComparingFieldByField(expected);
    }

    @Test
    void shouldConvertToMember() {
        OrganizationEntity organizationEntity = TestObjectFactory.buildOrganization();
        MemberEntity memberEntity = TestObjectFactory.testMemberEntity(TestObjectFactory.randomString());
        memberEntity.setOrganizations(Set.of(organizationEntity));

        User user = converter.toUser(memberEntity);

        assertEquals(memberEntity.getId(), user.getId());
        assertEquals(memberEntity.getEmail(), user.getEmail());
        assertEquals(memberEntity.getOrganizations().size(), user.getOrgs().size());
    }

    @Test
    void shouldConvertToOrganizationWithoutRoles() {
        OrganizationEntity organizationEntity = TestObjectFactory.buildOrganization();

        var organization = converter.toOrganization(organizationEntity, Collections.emptySet());

        assertEquals(organizationEntity.getId(), organization.getId());
        assertEquals(organizationEntity.getOwner(), organization.getOwner().getId());
        assertNull(organization.getRoles());
    }

    @Test
    void shouldConvertToOrganizationWithoutRolesAnotherOrganization() {
        OrganizationEntity organizationEntity = TestObjectFactory.buildOrganization();
        MemberRoleEntity memberRoleEntity =
                TestObjectFactory.buildMemberRole(RoleId.ADMINISTRATOR, TestObjectFactory.randomString());

        var organization = converter.toOrganization(organizationEntity, Set.of(memberRoleEntity));

        assertEquals(organizationEntity.getId(), organization.getId());
        assertEquals(organizationEntity.getOwner(), organization.getOwner().getId());
        assertEquals(organizationEntity.getId(), organization.getParty().getId());
        assertTrue(organization.getRoles().isEmpty());
    }

    @Test
    void shouldConvertToOrganization() {
        OrganizationEntity organizationEntity = TestObjectFactory.buildOrganization();
        MemberRoleEntity memberRoleEntity =
                TestObjectFactory.buildMemberRole(RoleId.ADMINISTRATOR, organizationEntity.getId());

        var organization = converter.toOrganization(organizationEntity, Set.of(memberRoleEntity));

        assertEquals(organizationEntity.getId(), organization.getId());
        assertEquals(organizationEntity.getOwner(), organization.getOwner().getId());
        assertEquals(organizationEntity.getId(), organization.getParty().getId());
        assertEquals(memberRoleEntity.getRoleId(), organization.getRoles().iterator().next().getId());
    }


}