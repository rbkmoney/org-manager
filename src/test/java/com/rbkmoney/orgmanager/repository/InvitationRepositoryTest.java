package com.rbkmoney.orgmanager.repository;

import com.rbkmoney.orgmanager.OrgManagerApplication;
import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.entity.RoleEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@SpringBootTest(classes = OrgManagerApplication.class)
@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = InvitationRepositoryTest.Initializer.class)
public class InvitationRepositoryTest extends AbstractRepositoryTest {

    private static final String ORGANIZATION_ID = "organization_Id";

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void shouldSaveInvitationWithRoles() {
        // Given
        InvitationEntity invitation = InvitationEntity.builder()
                .id("invitation_id")
                .acceptToken("accept_token")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now())
                .inviteeContactEmail("contact_email")
                .inviteeContactType("contact_type")
                .metadata("metadata")
                .organizationId(ORGANIZATION_ID)
                .inviteeRoles(Set.of(
                        RoleEntity.builder()
                                .id("role1_id")
                                .roleId("role1")
                                .resourceId("resource1")
                                .scopeId("scope1")
                                .organizationId(ORGANIZATION_ID)
                                .build(),
                        RoleEntity.builder()
                                .id("role2_id")
                                .roleId("role2")
                                .resourceId("resource2")
                                .scopeId("scope2")
                                .organizationId(ORGANIZATION_ID)
                                .build()))
                .build();

        // When
        invitationRepository.save(invitation);

        // Then
        List<InvitationEntity> invitations = invitationRepository.findByOrganizationId(ORGANIZATION_ID);
        assertThat(invitations).hasSize(1);

        List<RoleEntity> roles = roleRepository.findByOrganizationId(ORGANIZATION_ID);
        assertThat(roles).hasSize(2);
    }
}
