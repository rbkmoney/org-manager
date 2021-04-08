package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.TestObjectFactory;
import com.rbkmoney.orgmanager.converter.MemberConverter;
import com.rbkmoney.orgmanager.converter.OrganizationConverter;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.exception.ResourceNotFoundException;
import com.rbkmoney.orgmanager.repository.MemberRepository;
import com.rbkmoney.orgmanager.repository.OrganizationRepository;
import com.rbkmoney.swag.organizations.model.Member;
import com.rbkmoney.swag.organizations.model.MemberOrgListResult;
import com.rbkmoney.swag.organizations.model.Organization;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrganizationServiceTest {

    @Mock private OrganizationConverter organizationConverter;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private MemberConverter memberConverter;
    @Mock private MemberRepository memberRepository;

    @InjectMocks
    private OrganizationService service;

    @Test
    void shouldCreate() {
        // Given
        Organization organization = new Organization();
        OrganizationEntity entity = new OrganizationEntity();
        OrganizationEntity savedEntity = new OrganizationEntity();
        Organization savedOrganization = new Organization();

        when(organizationConverter.toEntity(organization, "testOwnerId"))
                .thenReturn(entity);
        when(organizationRepository.save(entity))
                .thenReturn(savedEntity);
        when(organizationConverter.toDomain(savedEntity))
                .thenReturn(savedOrganization);

        // When
        ResponseEntity<Organization> response = service.create("testOwnerId", organization, "");

        // Then
        verify(organizationRepository, times(1))
                .save(entity);
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
                .isEqualTo(savedOrganization);
    }

    @Test
    void shouldGet() {
        // Given
        String orgId = "orgId";
        OrganizationEntity entity = new OrganizationEntity();
        Organization organization = new Organization();

        when(organizationRepository.findById(orgId))
                .thenReturn(Optional.of(entity));
        when(organizationConverter.toDomain(entity))
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
    void shouldReturnNotFound() {
        // Given
        String orgId = "orgId";

        when(organizationRepository.findById(orgId))
                .thenReturn(Optional.empty());

        // When
        ResponseEntity<Organization> response = service.get(orgId);

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .isNull();
    }

    @Test
    void shouldListMembers() {
        // Given
        MemberEntity memberEntity = new MemberEntity();
        Member member = new Member();

        String orgId = "orgId";
        OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .members(Set.of(memberEntity))
                .build();

        when(organizationRepository.findById(orgId))
                .thenReturn(Optional.of(organizationEntity));
        when(memberConverter.toDomain(memberEntity))
                .thenReturn(member);

        // When
        ResponseEntity<MemberOrgListResult> response = service.listMembers(orgId);

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull();
        assertThat(response.getBody().getResult())
                .containsExactly(member);
    }

    @Test
    void shouldReturnNotFoundIfNoOrganizationExistForMembersList() {
        // Given
        String orgId = "orgId";

        when(organizationRepository.findById(orgId))
                .thenReturn(Optional.empty());

        // When
        ResponseEntity<MemberOrgListResult> response = service.listMembers(orgId);

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .isNull();
    }

    @Test
    void shouldThrowExceptionIfOrganizationDoesNotExist() {
        // Given
        String orgId = TestObjectFactory.randomString();
        String userId = TestObjectFactory.randomString();

        // When
        when(organizationRepository.findById(orgId))
                .thenReturn(Optional.empty());

        //Then
        assertThrows(ResourceNotFoundException.class, () -> service.getOrgMember(userId, orgId));
    }

    @Test
    void shouldThrowExceptionIfUserMotMemberOfOrganization() {
        // Given
        String orgId = TestObjectFactory.randomString();
        OrganizationEntity organizationEntity = new OrganizationEntity();
        organizationEntity.setId(orgId);
        String userId = TestObjectFactory.randomString();

        // When
        when(organizationRepository.findById(orgId))
                .thenReturn(Optional.of(organizationEntity));

        //Then
        assertThrows(ResourceNotFoundException.class, () -> service.getOrgMember(userId, orgId));
    }


    @Test
    void shouldGetOrgMember() {
        // Given
        String orgId = TestObjectFactory.randomString();
        OrganizationEntity organizationEntity = new OrganizationEntity();
        organizationEntity.setId(orgId);
        String userId = TestObjectFactory.randomString();
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setId(userId);
        organizationEntity.setMembers(Set.of(memberEntity));
        Member expectedMember = new Member();

        when(organizationRepository.findById(orgId))
                .thenReturn(Optional.of(organizationEntity));
        when(memberConverter.toDomain(memberEntity, Collections.emptyList()))
                .thenReturn(expectedMember);

        // When
        Member actualMember = service.getOrgMember(userId, orgId);

        // Then
        assertThat(actualMember)
                .isEqualTo(expectedMember);
    }
}
