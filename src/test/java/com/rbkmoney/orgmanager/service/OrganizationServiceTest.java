package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.TestObjectFactory;
import com.rbkmoney.orgmanager.converter.MemberConverter;
import com.rbkmoney.orgmanager.converter.OrganizationConverter;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.exception.PartyManagementException;
import com.rbkmoney.orgmanager.exception.ResourceNotFoundException;
import com.rbkmoney.orgmanager.repository.MemberRepository;
import com.rbkmoney.orgmanager.repository.OrganizationRepository;
import com.rbkmoney.orgmanager.service.dto.MemberWithRoleDto;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.rbkmoney.orgmanager.TestObjectFactory.testToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrganizationServiceTest {

    @Mock
    private OrganizationConverter organizationConverter;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private MemberConverter memberConverter;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PartyManagementService partyManagementService;

    @InjectMocks
    private OrganizationService service;

    private static final String OWNER_ID = "testOwnerId";
    private static final String EMAIL = "email@email.org";

    @Test
    void shouldThrowPartyManagementExceptionOnCreate() {
        // Given
        Organization organization = new Organization();
        OrganizationEntity entity = new OrganizationEntity();
        OrganizationEntity savedEntity = new OrganizationEntity();

        when(organizationConverter.toEntity(organization, OWNER_ID))
                .thenReturn(entity);
        when(organizationRepository.save(entity))
                .thenReturn(savedEntity);
        doThrow(new PartyManagementException())
                .when(partyManagementService).createParty(anyString(), anyString(), anyString());

        // When
        assertThrows(PartyManagementException.class,
                () -> service.create(testToken(OWNER_ID, EMAIL), organization, ""));

        // Then
        verify(organizationConverter, times(1))
                .toEntity(organization, OWNER_ID);
        verify(organizationRepository, times(1))
                .save(entity);
        verify(partyManagementService, times(1))
                .createParty(OWNER_ID, OWNER_ID, EMAIL);
        verify(organizationConverter, times(0))
                .toDomain(any(OrganizationEntity.class));
    }

    @Test
    void shouldCreate() {
        // Given
        Organization organization = new Organization();
        OrganizationEntity entity = new OrganizationEntity();
        OrganizationEntity savedEntity = new OrganizationEntity();
        Organization savedOrganization = new Organization();

        when(organizationConverter.toEntity(organization, OWNER_ID))
                .thenReturn(entity);
        when(organizationRepository.save(entity))
                .thenReturn(savedEntity);
        when(organizationConverter.toDomain(savedEntity))
                .thenReturn(savedOrganization);

        // When
        ResponseEntity<Organization> response = service.create(testToken(OWNER_ID, EMAIL), organization, "");

        // Then
        verify(organizationConverter, times(1))
                .toEntity(organization, OWNER_ID);
        verify(organizationRepository, times(1))
                .save(entity);
        verify(partyManagementService, times(1))
                .createParty(OWNER_ID, OWNER_ID, EMAIL);
        verify(organizationConverter, times(1))
                .toDomain(savedEntity);
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
                .isEqualTo(savedOrganization);
        assertThat(response.getBody().getParty())
                .isEqualTo(OWNER_ID);
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
        String orgId = TestObjectFactory.randomString();
        Member member = new Member();

        MemberWithRoleDto memberWithRoleDto = getMemberWithRoleDto();
        List<MemberWithRoleDto> memberWithRoleList = List.of(memberWithRoleDto);

        when(organizationRepository.existsById(orgId))
                .thenReturn(true);
        when(memberRepository.getOrgMemberList(orgId))
                .thenReturn(memberWithRoleList);
        when(memberConverter.toDomain(memberWithRoleList))
                .thenReturn(List.of(member));

        // When
        MemberOrgListResult response = service.listMembers(orgId);

        // Then
        assertThat(response)
                .isNotNull();
        assertThat(response.getResult())
                .containsExactly(member);
    }

    private MemberWithRoleDto getMemberWithRoleDto() {
        return new MemberWithRoleDto() {
            @Override
            public String getId() {
                return TestObjectFactory.randomString();
            }

            @Override
            public String getEmail() {
                return TestObjectFactory.randomString();
            }

            @Override
            public String getMemberRoleId() {
                return TestObjectFactory.randomString();
            }

            @Override
            public String getOrganizationId() {
                return TestObjectFactory.randomString();
            }

            @Override
            public String getRoleId() {
                return TestObjectFactory.randomString();
            }

            @Override
            public String getScopeId() {
                return TestObjectFactory.randomString();
            }

            @Override
            public String getResourceId() {
                return TestObjectFactory.randomString();
            }
        };
    }

    @Test
    void shouldReturnNotFoundIfNoOrganizationExistForMembersList() {
        // Given
        String orgId = "orgId";
        // When
        when(organizationRepository.existsById(orgId))
                .thenReturn(false);
        // Then
        assertThrows(ResourceNotFoundException.class, () -> service.listMembers(orgId));
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
    void shouldThrowExceptionIfUserNotMemberOfOrganization() {
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
