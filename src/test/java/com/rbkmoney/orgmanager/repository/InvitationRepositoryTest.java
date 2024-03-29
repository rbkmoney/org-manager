package com.rbkmoney.orgmanager.repository;

import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InvitationRepositoryTest extends AbstractRepositoryTest {

    private static final String ORGANIZATION_ID = "orgId";

    @Test
    void shouldSaveInvitationWithRoles() {
        // Given
        String invitationId = "invitationId";

        InvitationEntity invitation = InvitationEntity.builder()
                .id(invitationId)
                .acceptToken("token")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now())
                .inviteeContactEmail("contactEmail")
                .inviteeContactType("contactType")
                .metadata("metadata")
                .organizationId(ORGANIZATION_ID)
                .status("Pending")
                .inviteeRoles(Set.of(
                        MemberRoleEntity.builder()
                                .id("role1")
                                .roleId("role1")
                                .resourceId("resource1")
                                .scopeId("scope1")
                                .organizationId(ORGANIZATION_ID)
                                .build(),
                        MemberRoleEntity.builder()
                                .id("role2")
                                .roleId("role2")
                                .resourceId("resource2")
                                .scopeId("scope2")
                                .organizationId(ORGANIZATION_ID)
                                .build()))
                .build();

        // When
        invitationRepository.save(invitation);

        // Then
        Optional<InvitationEntity> savedInvitation = invitationRepository.findById(invitationId);
        assertTrue(savedInvitation.isPresent());
        assertThat(savedInvitation.get().getInviteeRoles()).hasSize(2);

        List<MemberRoleEntity> savedRoles = memberRoleRepository.findByOrganizationId(ORGANIZATION_ID);
        assertThat(savedRoles).hasSize(2);
    }
}
