package com.rbkmoney.orgmanager.repository;

import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.entity.OrganizationRoleEntity;
import com.rbkmoney.orgmanager.entity.ScopeEntity;
import com.rbkmoney.orgmanager.service.OrganizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
public class OrganizationRepositoryTest extends AbstractRepositoryTest {

    private static final String ORGANIZATION_ID = "orgId";

    @Autowired
    private OrganizationService organizationService;

    @Test
    void shouldModifyOrganization() {
        // Given
        MemberEntity member = MemberEntity.builder()
                .id("memberId")
                .email("email")
                .build();
        OrganizationEntity organization = OrganizationEntity.builder()
                .id(ORGANIZATION_ID)
                .createdAt(LocalDateTime.now())
                .name("name")
                .owner("owner")
                .members(Set.of(member))
                .build();

        // When
        organizationRepository.save(organization);

        String modifyOrgName = "testOrgName";
        organizationService.modify(ORGANIZATION_ID, modifyOrgName);

        Optional<OrganizationEntity> organizationEntityOptional = organizationRepository.findById(ORGANIZATION_ID);
        assertTrue(organizationEntityOptional.isPresent());
        assertEquals(modifyOrgName, organizationEntityOptional.get().getName());
    }

    @Test
    void shouldSaveOrganizationWithMembers() {
        // Given
        MemberEntity member = MemberEntity.builder()
                .id("memberId")
                .email("email")
                .build();
        OrganizationEntity organization = OrganizationEntity.builder()
                .id(ORGANIZATION_ID)
                .createdAt(LocalDateTime.now())
                .name("name")
                .owner("owner")
                .members(Set.of(member))
                .build();

        // When
        organizationRepository.save(organization);

        // Then
        Optional<OrganizationEntity> savedOrganization = organizationRepository.findById(ORGANIZATION_ID);
        assertTrue(savedOrganization.isPresent());
        assertThat(savedOrganization.get().getMembers()).hasSize(1);

        Optional<MemberEntity> savedMember = memberRepository.findById("memberId");
        assertTrue(savedMember.isPresent());
    }

    @Test
    void shouldSaveOrganizationWithRoles() {
        // Given
        ScopeEntity scope = ScopeEntity.builder()
                .id("Shop")
                .build();

        OrganizationRoleEntity role = OrganizationRoleEntity.builder()
                .id("roleId")
                .roleId("Administrator")
                .name("name")
                .organizationId(ORGANIZATION_ID)
                .possibleScopes(Set.of(scope))
                .build();

        OrganizationEntity organization = OrganizationEntity.builder()
                .id(ORGANIZATION_ID)
                .createdAt(LocalDateTime.now())
                .name("name")
                .owner("owner")
                .roles(Set.of(role))
                .build();

        // When
        organizationRepository.save(organization);

        // Then
        Optional<OrganizationEntity> savedOrganizationOptional = organizationRepository.findById(ORGANIZATION_ID);
        assertTrue(savedOrganizationOptional.isPresent());
        assertThat(savedOrganizationOptional.get().getRoles()).hasSize(1);
        savedOrganizationOptional.get().getRoles().forEach(
                r -> assertThat(r.getPossibleScopes()).hasSize(1));

        Optional<OrganizationRoleEntity> savedMember = organizationRoleRepository.findById("roleId");
        assertTrue(savedMember.isPresent());
    }
}
