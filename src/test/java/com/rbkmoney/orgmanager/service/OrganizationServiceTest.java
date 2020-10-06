package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.converter.OrganizationConverter;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.repository.OrganizationRepository;
import com.rbkmoney.swag.organizations.model.Organization;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationServiceTest {

    @Mock private OrganizationConverter converter;
    @Mock private OrganizationRepository repository;

    @InjectMocks
    private OrganizationService service;

    @Test
    public void shouldCreate() {
        // Given
        Organization organization = new Organization();
        OrganizationEntity entity = new OrganizationEntity();
        OrganizationEntity savedEntity = new OrganizationEntity();
        Organization savedOrganization = new Organization();

        when(converter.toEntity(organization))
                .thenReturn(entity);
        when(repository.save(entity))
                .thenReturn(savedEntity);
        when(converter.toDomain(savedEntity))
                .thenReturn(savedOrganization);

        // When
        ResponseEntity<Organization> response = service.create(organization, "");

        // Then
        verify(repository, times(1))
                .save(entity);
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
                .isEqualTo(savedOrganization);
    }

    @Test
    public void shouldGet() {
        // Given
        String orgId = "orgId";
        OrganizationEntity entity = new OrganizationEntity();
        Organization organization = new Organization();

        when(repository.findById(orgId))
                .thenReturn(Optional.of(entity));
        when(converter.toDomain(entity))
                .thenReturn(organization);

        // When
        ResponseEntity<Organization> response = service.get(orgId);

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isEqualTo(organization);
    }

    @Test
    public void shouldReturnNotFound() {
        // Given
        String orgId = "orgId";

        when(repository.findById(orgId))
                .thenReturn(Optional.empty());

        // When
        ResponseEntity<Organization> response = service.get(orgId);

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .isNull();
    }
}
