package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.converter.MemberConverter;
import com.rbkmoney.orgmanager.converter.OrganizationConverter;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.repository.MemberRepository;
import com.rbkmoney.orgmanager.repository.OrganizationRepository;
import com.rbkmoney.swag.organizations.model.InlineResponse2001;
import com.rbkmoney.swag.organizations.model.InlineResponse2002;
import com.rbkmoney.swag.organizations.model.Member;
import com.rbkmoney.swag.organizations.model.Organization;
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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationServiceTest {

    @Mock private OrganizationConverter organizationConverter;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private MemberConverter memberConverter;
    @Mock private MemberRepository memberRepository;

    @InjectMocks
    private OrganizationService service;

    @Test
    public void shouldCreate() {
        // Given
        Organization organization = new Organization();
        OrganizationEntity entity = new OrganizationEntity();
        OrganizationEntity savedEntity = new OrganizationEntity();
        Organization savedOrganization = new Organization();

        when(organizationConverter.toEntity(organization))
                .thenReturn(entity);
        when(organizationRepository.save(entity))
                .thenReturn(savedEntity);
        when(organizationConverter.toDomain(savedEntity))
                .thenReturn(savedOrganization);

        // When
        ResponseEntity<Organization> response = service.create(organization, "");

        // Then
        verify(organizationRepository, times(1))
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
    public void shouldReturnNotFound() {
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
    public void shouldListMembers() {
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
        ResponseEntity<InlineResponse2001> response = service.listMembers(orgId);

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull();
        assertThat(response.getBody().getResults())
                .containsExactly(member);
    }

    @Test
    public void shouldReturnNotFoundIfNoOrganizationExistForMembersList() {
        // Given
        String orgId = "orgId";

        when(organizationRepository.findById(orgId))
                .thenReturn(Optional.empty());

        // When
        ResponseEntity<InlineResponse2001> response = service.listMembers(orgId);

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .isNull();
    }

    @Test
    public void shouldFindMemberById() {
        // Given
        MemberEntity memberEntity = new MemberEntity();
        Member member = new Member();

        String userId = "userId";

        when(memberRepository.findById(userId))
                .thenReturn(Optional.of(memberEntity));
        when(memberConverter.toDomain(memberEntity))
                .thenReturn(member);

        // When
        ResponseEntity<Member> response = service.getMember(userId);

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isEqualTo(member);
    }

    @Test
    public void shouldReturnNotFoundIfMemberDoesNotExist() {
        // Given
        String userId = "userId";

        when(memberRepository.findById(userId))
                .thenReturn(Optional.empty());

        // When
        ResponseEntity<Member> response = service.getMember(userId);

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .isNull();
    }
}
