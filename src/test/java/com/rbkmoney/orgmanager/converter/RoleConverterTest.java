package com.rbkmoney.orgmanager.converter;

import com.rbkmoney.orgmanager.entity.RoleEntity;
import com.rbkmoney.swag.organizations.model.MemberRole;
import com.rbkmoney.swag.organizations.model.MemberRoleScope;
import com.rbkmoney.swag.organizations.model.ResourceScopeId;
import com.rbkmoney.swag.organizations.model.RoleId;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RoleConverterTest {

    private RoleConverter converter;

    @Before
    public void setUp() {
        converter = new RoleConverter();
    }

    @Test
    public void shouldConvertToEntity() {
        // Given
        MemberRole role = new MemberRole()
                .roleId(RoleId.ADMINISTRATOR)
                .scope(new MemberRoleScope()
                        .resourceId("resource")
                        .id(ResourceScopeId.SHOP));

        // When
        RoleEntity entity = converter.toEntity(role, "org");

        // Then
        RoleEntity expected = RoleEntity.builder()
                .roleId("Administrator")
                .scopeId("Shop")
                .resourceId("resource")
                .organizationId("org")
                .build();

        assertThat(entity.getId()).isNotEmpty();
        assertThat(entity).isEqualToIgnoringNullFields(expected);
    }

    @Test
    public void shouldConvertToDomain() {
        // Given
        RoleEntity entity = RoleEntity.builder()
                .id("id")
                .roleId("Administrator")
                .scopeId("Shop")
                .resourceId("resource")
                .organizationId("org")
                .build();

        // When
        MemberRole role = converter.toDomain(entity);

        // Then
        MemberRole expected = new MemberRole()
                .roleId(RoleId.ADMINISTRATOR)
                .scope(new MemberRoleScope()
                        .resourceId("resource")
                        .id(ResourceScopeId.SHOP));

        assertThat(role).isEqualToComparingFieldByField(expected);
    }
}