package com.rbkmoney.orgmanager.converter;

import com.rbkmoney.orgmanager.entity.OrganizationRoleEntity;
import com.rbkmoney.orgmanager.entity.ScopeEntity;
import com.rbkmoney.swag.organizations.model.ResourceScopeId;
import com.rbkmoney.swag.organizations.model.Role;
import com.rbkmoney.swag.organizations.model.RoleId;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class OrganizationRoleConverterTest {

    private OrganizationRoleConverter converter;

    @Before
    public void setUp() {
        converter = new OrganizationRoleConverter();
    }

    @Test
    public void shouldConvertToDomain() {
        // Given
        OrganizationRoleEntity entity = OrganizationRoleEntity.builder()
                .id("id")
                .organizationId("orgId")
                .name("name")
                .roleId("Administrator")
                .possibleScopes(Set.of(ScopeEntity.builder()
                        .id("Shop")
                        .build()))
                .build();

        // When
        Role role = converter.toDomain(entity);

        // Then
        Role expected = new Role()
                .id(RoleId.ADMINISTRATOR)
                .name("name")
                .scopes(Set.of(ResourceScopeId.SHOP));

        assertThat(role).isEqualToComparingFieldByField(expected);
    }
}
