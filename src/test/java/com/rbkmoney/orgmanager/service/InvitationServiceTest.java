package com.rbkmoney.orgmanager.service;

import com.rbkmoney.damsel.message_sender.MessageSenderSrv;
import com.rbkmoney.orgmanager.converter.InvitationConverter;
import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.repository.InvitationRepository;
import com.rbkmoney.orgmanager.repository.OrganizationRepository;
import com.rbkmoney.swag.organizations.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InvitationServiceTest {

    @Mock private InvitationConverter invitationConverter;
    @Mock private InvitationRepository invitationRepository;
    @Mock private OrganizationRepository organizationRepository;
    @MockBean
    private MessageSenderSrv.Iface dudoserClient;

    @InjectMocks
    private InvitationService service;

    @Test
    public void shouldCreate() {
        // Given
        InvitationRequest invitation = new InvitationRequest();
        InvitationEntity entity = new InvitationEntity();
        InvitationEntity savedEntity = new InvitationEntity();
        Invitation savedInvitation = new Invitation();

        when(invitationConverter.toEntity(invitation, "org"))
                .thenReturn(entity);
        when(invitationRepository.save(entity))
                .thenReturn(savedEntity);
        when(invitationConverter.toDomain(savedEntity))
                .thenReturn(savedInvitation);

        // When
        ResponseEntity<Invitation> response = service.create("org", invitation, "");

        // Then
        verify(invitationRepository, times(1))
                .save(entity);
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
                .isEqualTo(savedInvitation);
    }

    @Test
    public void shouldGet() {
        // Given
        String invitationId = "invitationId";
        InvitationEntity entity = new InvitationEntity();
        Invitation invitation = new Invitation();

        when(invitationRepository.findById(invitationId))
                .thenReturn(Optional.of(entity));
        when(invitationConverter.toDomain(entity))
                .thenReturn(invitation);

        // When
        ResponseEntity<Invitation> response = service.get(invitationId);

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isEqualTo(invitation);
    }

    @Test
    public void shouldReturnNotFound() {
        // Given
        String invitationId = "invitationId";

        when(invitationRepository.findById(invitationId))
                .thenReturn(Optional.empty());

        // When
        ResponseEntity<Invitation> response = service.get(invitationId);

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .isNull();
    }

    @Test
    public void shouldFindByOrganizationIdAndStatus() {
        // Given
        String orgId = "orgId";

        InvitationEntity entity = new InvitationEntity();
        Invitation invitation = new Invitation();

        when(organizationRepository.existsById(orgId))
                .thenReturn(true);
        when(invitationRepository.findByOrganizationIdAndStatus(orgId, "Pending"))
                .thenReturn(List.of(entity));
        when(invitationConverter.toDomain(entity))
                .thenReturn(invitation);

        // When
        ResponseEntity<InvitationListResult> response = service.list(orgId, InvitationStatusName.PENDING);

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull();
        assertThat(response.getBody().getResult())
                .containsExactly(invitation);
    }

    @Test
    public void shouldReturnNotFoundIfOrganizationDoesNotExist() {
        // Given
        String orgId = "orgId";
        when(organizationRepository.existsById(orgId))
                .thenReturn(false);

        // When
        ResponseEntity<InvitationListResult> response = service.list(orgId, InvitationStatusName.ACCEPTED);

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .isNull();
    }

    @Test
    public void shouldRevoke() {
        // Given
        String orgId = "orgId";
        String invitationId = "invitationId";
        InvitationEntity entity = new InvitationEntity();

        when(invitationRepository.findById(invitationId))
                .thenReturn(Optional.of(entity));

        // When
        ResponseEntity<Void> response = service.revoke(orgId, invitationId, new InlineObject1()
                .reason("reason")
                .status(InlineObject1.StatusEnum.REVOKED));

        // Then
        assertThat(entity.getStatus())
                .isEqualTo("Revoked");
        assertThat(entity.getRevocationReason())
                .isEqualTo("reason");
        verify(invitationRepository, times(1))
                .save(entity);
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void shouldReturnNotFoundIfInvitationDoesNotExist() {
        // Given
        String orgId = "orgId";
        String invitationId = "invitationId";

        when(invitationRepository.findById(invitationId))
                .thenReturn(Optional.empty());

        // When
        ResponseEntity<Void> response = service.revoke(orgId, invitationId, new InlineObject1());

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
