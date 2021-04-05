package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.repository.AbstractRepositoryTest;
import com.rbkmoney.orgmanager.util.TestData;
import com.rbkmoney.swag.organizations.model.InvitationListResult;
import com.rbkmoney.swag.organizations.model.InvitationStatusName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

}
