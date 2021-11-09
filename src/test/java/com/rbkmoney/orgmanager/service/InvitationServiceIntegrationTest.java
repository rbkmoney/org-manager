package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.TestObjectFactory;
import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.repository.AbstractRepositoryTest;
import com.rbkmoney.orgmanager.util.TestData;
import com.rbkmoney.swag.organizations.model.Invitation;
import com.rbkmoney.swag.organizations.model.InvitationListResult;
import com.rbkmoney.swag.organizations.model.InvitationStatusName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InvitationServiceIntegrationTest extends AbstractRepositoryTest {

    private static final String INVITATION_ID = "invitationId";

    private static final String ORGANIZATION_ID = "orgId";

    private static final String MEMBER_ID = "memberId";

    @Autowired
    private InvitationService invitationService;

    @Test
    void checkOnInvitationStatus() {
        InvitationEntity expiredInvite =
                TestData.buildInvitation(ORGANIZATION_ID, INVITATION_ID, LocalDateTime.now().minusDays(1));
        invitationRepository.save(expiredInvite);
        invitationService.checkAndModifyExpiredStatus();
        expiredInvite = invitationRepository.findById(INVITATION_ID).get();
        assertEquals(InvitationStatusName.EXPIRED.getValue(), expiredInvite.getStatus());
    }

    @Test
    void checkExpiredInviteWithPendingStatus() {
        OrganizationEntity organizationEntity = TestData.buildOrganization(ORGANIZATION_ID, MEMBER_ID);
        organizationRepository.save(organizationEntity);

        InvitationEntity invite =
                TestData.buildInvitation(ORGANIZATION_ID, INVITATION_ID, LocalDateTime.now().plusDays(1));
        InvitationEntity expiredInvite =
                TestData.buildInvitation(ORGANIZATION_ID, INVITATION_ID + "_2", LocalDateTime.now().minusDays(1));
        invitationRepository.saveAll(List.of(invite, expiredInvite));

        ResponseEntity<InvitationListResult> responseEntity =
                invitationService.list(ORGANIZATION_ID, InvitationStatusName.PENDING);
        assertEquals(1, responseEntity.getBody().getResult().size());
    }

    @Test
    void checkInviteWithEmptyStatusRequest() {
        OrganizationEntity organizationEntity = TestData.buildOrganization(ORGANIZATION_ID, MEMBER_ID);
        organizationRepository.save(organizationEntity);

        final InvitationEntity invite =
                TestData.buildInvitation(ORGANIZATION_ID, INVITATION_ID + "_1", LocalDateTime.now().plusDays(1));
        final InvitationEntity expiredInvite =
                TestData.buildInvitation(ORGANIZATION_ID, INVITATION_ID + "_2", LocalDateTime.now().minusDays(1));
        final InvitationEntity acceptedInvite =
                TestData.buildInvitation(
                        ORGANIZATION_ID,
                        INVITATION_ID + "_3",
                        LocalDateTime.now().plusDays(1),
                        InvitationStatusName.ACCEPTED
                );
        acceptedInvite.setAcceptedAt(LocalDateTime.now().plusHours(2));
        acceptedInvite.setAcceptedMemberId(TestObjectFactory.randomString());
        final InvitationEntity revokedInvite =
                TestData.buildInvitation(
                        ORGANIZATION_ID,
                        INVITATION_ID + "_4",
                        LocalDateTime.now().plusDays(1),
                        InvitationStatusName.REVOKED
                );
        revokedInvite.setRevokedAt(LocalDateTime.now().plusHours(2));
        revokedInvite.setRevocationReason(TestObjectFactory.randomString());
        invitationRepository.saveAll(List.of(invite, expiredInvite, acceptedInvite, revokedInvite));

        ResponseEntity<InvitationListResult> responseEntity =
                invitationService.list(ORGANIZATION_ID, null);
        Map<String, Invitation> invites = responseEntity.getBody().getResult().stream()
                .collect(Collectors.toMap(
                        Invitation::getId,
                        invitation -> invitation
                ));
        assertEquals(3, invites.size());
        assertNotNull(invites.get(INVITATION_ID + "_1"));
        assertNotNull(invites.get(INVITATION_ID + "_3"));
        assertNotNull(invites.get(INVITATION_ID + "_4"));
    }
}
