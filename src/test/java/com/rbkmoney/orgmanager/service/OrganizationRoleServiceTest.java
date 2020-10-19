package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.converter.OrganizationRoleConverter;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.entity.OrganizationRoleEntity;
import com.rbkmoney.orgmanager.repository.OrganizationRepository;
import com.rbkmoney.orgmanager.repository.OrganizationRoleRepository;
import com.rbkmoney.swag.organizations.model.InlineResponse2001;
import com.rbkmoney.swag.organizations.model.Role;
import com.rbkmoney.swag.organizations.model.RoleId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationRoleServiceTest {

    @Mock private OrganizationRepository organizationRepository;
    @Mock private OrganizationRoleRepository organizationRoleRepository;
    @Mock private OrganizationRoleConverter organizationRoleConverter;

    @InjectMocks
    private OrganizationRoleService service;

    @Test
    public void shouldListRoles() {
        // Given
        OrganizationRoleEntity organizationRoleEntity = new OrganizationRoleEntity();
        Role role = new Role();

        String orgId = "orgId";
        OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .roles(Set.of(organizationRoleEntity))
                .build();

        when(organizationRepository.findById(orgId))
                .thenReturn(Optional.of(organizationEntity));
        when(organizationRoleConverter.toDomain(organizationRoleEntity))
                .thenReturn(role);

        // When
        ResponseEntity<InlineResponse2001> response = service.list(orgId);

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull();
        assertThat(response.getBody().getResults())
                .containsExactly(role);
    }

    @Test
    public void shouldReturnNotFoundIfNoOrganizationExistForRolesList() {
        // Given
        String orgId = "orgId";

        when(organizationRepository.findById(orgId))
                .thenReturn(Optional.empty());

        // When
        ResponseEntity<InlineResponse2001> response = service.list(orgId);

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .isNull();
    }

    @Test
    public void shouldFindRoleById() {
        // Given
        OrganizationRoleEntity organizationRoleEntity = new OrganizationRoleEntity();
        Role role = new Role();

        String orgId = "orgId";
        RoleId roleId = RoleId.ADMINISTRATOR;

        when(organizationRepository.existsById(orgId))
                .thenReturn(true);
        when(organizationRoleRepository.findByOrganizationIdAndRoleId(orgId, roleId.getValue()))
                .thenReturn(Optional.of(organizationRoleEntity));
        when(organizationRoleConverter.toDomain(organizationRoleEntity))
                .thenReturn(role);

        // When
        ResponseEntity<Role> response = service.get(orgId, roleId);

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isEqualTo(role);
    }

    @Test
    public void shouldReturnNotFoundIfOrganizationDoesNotExist() {
        // Given
        String orgId = "orgId";
        RoleId roleId = RoleId.ADMINISTRATOR;

        when(organizationRepository.existsById(orgId))
                .thenReturn(false);

        // When
        ResponseEntity<Role> response = service.get(orgId, roleId);

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .isNull();
    }
}