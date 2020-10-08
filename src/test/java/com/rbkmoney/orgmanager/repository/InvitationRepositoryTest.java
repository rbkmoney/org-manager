package com.rbkmoney.orgmanager.repository;

import com.rbkmoney.orgmanager.OrgManagerApplication;
import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@SpringBootTest(classes = OrgManagerApplication.class)
@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = InvitationRepositoryTest.Initializer.class)
public class InvitationRepositoryTest extends AbstractRepositoryTest {

    private static final String ORGANIZATION_ID = "orgId";

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private MemberRoleRepository memberRoleRepository;

    @Test
    public void shouldSaveInvitationWithRoles() {
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
        assertThat(savedInvitation.isPresent());

        List<MemberRoleEntity> roles = memberRoleRepository.findByOrganizationId(ORGANIZATION_ID);
        assertThat(roles).hasSize(2);
    }
}
