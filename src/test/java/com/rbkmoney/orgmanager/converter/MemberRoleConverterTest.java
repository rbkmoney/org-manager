package com.rbkmoney.orgmanager.converter;

import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.swag.organizations.model.MemberRole;
import com.rbkmoney.swag.organizations.model.MemberRoleScope;
import com.rbkmoney.swag.organizations.model.ResourceScopeId;
import com.rbkmoney.swag.organizations.model.RoleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MemberRoleConverterTest {

    private MemberRoleConverter converter;

    @BeforeEach
    public void setUp() {
        converter = new MemberRoleConverter();
    }

    @Test
    void shouldConvertToEntityWithoutScope() {
        // Given
        MemberRole role = new MemberRole()
                .roleId(RoleId.ADMINISTRATOR);

        // When
        MemberRoleEntity entity = converter.toEntity(role, "org");

        // Then
        MemberRoleEntity expected = MemberRoleEntity.builder()
                .roleId("Administrator")
                .organizationId("org")
                .build();

        assertThat(entity.getId()).isNotEmpty();
        assertThat(entity).isEqualToIgnoringNullFields(expected);
    }

    @Test
    void shouldConvertToEntity() {
        // Given
        MemberRole role = new MemberRole()
                .roleId(RoleId.ADMINISTRATOR)
                .scope(new MemberRoleScope()
                        .resourceId("resource")
                        .id(ResourceScopeId.SHOP));

        // When
        MemberRoleEntity entity = converter.toEntity(role, "org");

        // Then
        MemberRoleEntity expected = MemberRoleEntity.builder()
                .roleId("Administrator")
                .scopeId("Shop")
                .resourceId("resource")
                .organizationId("org")
                .build();

        assertThat(entity.getId()).isNotEmpty();
        assertThat(entity).isEqualToIgnoringNullFields(expected);
    }

    @Test
    void shouldConvertToDomainWithoutScope() {
        // Given
        MemberRoleEntity entity = MemberRoleEntity.builder()
                .id("id")
                .roleId("Administrator")
                .organizationId("org")
                .build();

        // When
        MemberRole role = converter.toDomain(entity);

        // Then
        MemberRole expected = new MemberRole()
                .id(entity.getId())
                .roleId(RoleId.ADMINISTRATOR);

        assertThat(role).isEqualToComparingFieldByField(expected);
    }

    @Test
    void shouldConvertToDomain() {
        // Given
        MemberRoleEntity entity = MemberRoleEntity.builder()
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
                .id(entity.getId())
                .roleId(RoleId.ADMINISTRATOR)
                .scope(new MemberRoleScope()
                        .resourceId("resource")
                        .id(ResourceScopeId.SHOP));

        assertThat(role).isEqualToComparingFieldByField(expected);
    }
}