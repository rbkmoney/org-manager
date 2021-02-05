package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.OrgManagerApplication;
import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.repository.AbstractRepositoryTest;
import com.rbkmoney.orgmanager.repository.InvitationRepository;
import com.rbkmoney.orgmanager.repository.InvitationRepositoryTest;
import com.rbkmoney.orgmanager.repository.OrganizationRepository;
import com.rbkmoney.orgmanager.util.JsonMapper;
import com.rbkmoney.orgmanager.util.TestData;
import com.rbkmoney.swag.organizations.model.InvitationListResult;
import com.rbkmoney.swag.organizations.model.InvitationStatusName;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;

@DirtiesContext
@SpringBootTest(classes = OrgManagerApplication.class)
@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = InvitationRepositoryTest.Initializer.class)
public class InvitationServiceIntegrationTest extends AbstractRepositoryTest {

    private static final String INVITATION_ID = "invitationId";

    private static final String ORGANIZATION_ID = "orgId";

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    public void checkOnInvitationStatus() {
        InvitationEntity expiredInvite =
                TestData.buildInvitation(ORGANIZATION_ID, INVITATION_ID, LocalDateTime.now().minusDays(1));
        invitationRepository.save(expiredInvite);
        invitationService.checkAndModifyExpiredStatus();
        expiredInvite = invitationRepository.findById(INVITATION_ID).get();
        Assert.assertEquals(InvitationStatusName.EXPIRED.getValue(), expiredInvite.getStatus());
    }

    @Test
    public void checkExpiredInviteWithPendingStatus() {
        OrganizationEntity organizationEntity = TestData.buildOrganization(ORGANIZATION_ID);
        organizationRepository.save(organizationEntity);

        InvitationEntity invite =
                TestData.buildInvitation(ORGANIZATION_ID, INVITATION_ID, LocalDateTime.now().plusDays(1));
        InvitationEntity expiredInvite =
                TestData.buildInvitation(ORGANIZATION_ID, INVITATION_ID + "_2", LocalDateTime.now().minusDays(1));
        invitationRepository.saveAll(List.of(invite, expiredInvite));

        ResponseEntity<InvitationListResult> responseEntity =
                invitationService.list(ORGANIZATION_ID, InvitationStatusName.PENDING);
        Assert.assertEquals(1, responseEntity.getBody().getResult().size());
    }

}
