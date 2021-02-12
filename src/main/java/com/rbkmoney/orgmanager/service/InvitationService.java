package com.rbkmoney.orgmanager.service;

import com.rbkmoney.damsel.message_sender.MailBody;
import com.rbkmoney.damsel.message_sender.Message;
import com.rbkmoney.damsel.message_sender.MessageMail;
import com.rbkmoney.damsel.message_sender.MessageSenderSrv;
import com.rbkmoney.orgmanager.converter.InvitationConverter;
import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.repository.InvitationRepository;
import com.rbkmoney.orgmanager.repository.OrganizationRepository;
import com.rbkmoney.swag.organizations.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationService {

    private final InvitationConverter invitationConverter;
    private final InvitationRepository invitationRepository;
    private final OrganizationRepository organizationRepository;
    private final MessageSenderSrv.Iface dudoserClient;

    // TODO [a.romanov]: idempotency
    public ResponseEntity<Invitation> create(
            String orgId,
            InvitationRequest invitation,
            String xIdempotencyKey) {
        InvitationEntity entity = invitationConverter.toEntity(invitation, orgId);
        InvitationEntity savedEntity = invitationRepository.save(entity);

        Invitation savedInvitation = invitationConverter.toDomain(savedEntity);

        try {
            MessageMail messageMail = new MessageMail();
            messageMail.setMailBody(new MailBody("https://dashboard.rbk.money/organizations/accept-invitation/" + savedInvitation.getAcceptToken()));
            messageMail.setToEmails(List.of(savedInvitation.getInvitee().getContact().getEmail()));
            messageMail.setSubject("Invitee");
            messageMail.setFromEmail("no-reply@rbkmoney.com");

            dudoserClient.send(Message.message_mail(messageMail));
        } catch (Exception ex) {
            log.warn("dudoserClient error", ex);
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedInvitation);
    }

    public ResponseEntity<Invitation> get(String invitationId) {
        Optional<InvitationEntity> entity = invitationRepository.findById(invitationId);

        if (entity.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Invitation invitation = invitationConverter.toDomain(entity.get());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(invitation);
    }

    public ResponseEntity<InvitationListResult> list(String orgId, InvitationStatusName status) {
        boolean isOrganizationExist = organizationRepository.existsById(orgId);

        if (!isOrganizationExist) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        List<InvitationEntity> entities = invitationRepository.findByOrganizationIdAndStatus(orgId, status.getValue());
        if (status == InvitationStatusName.PENDING) {
            // Additional check if invitation already expired
            entities = entities.stream().filter(invitationEntity -> !invitationEntity.isExpired())
                    .collect(Collectors.toList());
        }

        List<Invitation> invitations = entities.stream()
                .map(invitationConverter::toDomain)
                .collect(toList());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new InvitationListResult()
                        .result(invitations));
    }

    public ResponseEntity<Void> revoke(String orgId, String invitationId, InlineObject1 inlineObject) {
        Optional<InvitationEntity> entity = invitationRepository.findById(invitationId);

        if (entity.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        InvitationEntity updatedEntity = entity.get();
        updatedEntity.setStatus(inlineObject.getStatus().getValue());
        updatedEntity.setRevocationReason(inlineObject.getReason());
        updatedEntity.setRevokedAt(LocalDateTime.now());

        invitationRepository.save(updatedEntity);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @Transactional
    public void checkAndModifyExpiredStatus() {
        Stream<InvitationEntity> invitationEntity = invitationRepository.findAllPendingStatus();
        invitationEntity.forEach(invitation -> {
            if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
                invitation.setStatus(InvitationStatusName.EXPIRED.getValue());
            }
        });
    }

}
