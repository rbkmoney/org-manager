package com.rbkmoney.orgmanager.converter;

import com.rbkmoney.bouncer.context.v1.User;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.swag.organizations.model.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MemberConverterTest {

    private MemberConverter converter;

    @Before
    public void setUp() {
        MemberRoleConverter memberRoleConverter = mock(MemberRoleConverter.class);
        when(memberRoleConverter.toDomain(any(MemberRoleEntity.class)))
                .thenReturn(new MemberRole());
        when(memberRoleConverter.toEntity(any(MemberRole.class), anyString()))
                .thenReturn(new MemberRoleEntity());

        OrganizationConverter organizationConverter = mock(OrganizationConverter.class);
        when(organizationConverter.toThrift(any(OrganizationEntity.class)))
                .thenReturn(new com.rbkmoney.bouncer.context.v1.Organization());

        converter = new MemberConverter(memberRoleConverter, organizationConverter);
    }

    @Test
    public void shouldConvertToDomain() {
        // Given
        MemberEntity entity = buildMemberEntity();

        // When
        Member member = converter.toDomain(entity);

        // Then
        Member expected = new Member()
                .id("id")
                .userEmail("email")
                .roles(Set.of(new MemberRole()));

        assertThat(member).isEqualToComparingFieldByField(expected);
    }

    @Test
    public void shouldConvertToThrift() {
        MemberEntity entity = buildMemberEntity();
        User user = converter.toThrift(entity);
        Assert.assertEquals(entity.getId(), user.getId());
        Assert.assertEquals(entity.getEmail(), user.getEmail());
        Assert.assertEquals(entity.getOrganizations().size(), user.getOrgs().size());
    }

    private MemberEntity buildMemberEntity() {
        return MemberEntity.builder()
                .id("id")
                .email("email")
                .roles(Set.of(new MemberRoleEntity()))
                .organizations(Set.of(new OrganizationEntity()))
                .build();
    }

}