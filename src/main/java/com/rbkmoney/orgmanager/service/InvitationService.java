package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.converter.InvitationConverter;
import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.exception.InviteExpiredException;
import com.rbkmoney.orgmanager.exception.InviteRevokedException;
import com.rbkmoney.orgmanager.exception.ResourceNotFoundException;
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
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class InvitationService {

    private final InvitationConverter invitationConverter;
    private final InvitationRepository invitationRepository;
    private final OrganizationRepository organizationRepository;
    private final MailInviteMessageSender mailInviteMessageSender;

    // TODO [a.romanov]: idempotency
    @Transactional
    public ResponseEntity<Invitation> create(
            String orgId,
            InvitationRequest invitation,
            String idempotencyKey) {
        InvitationEntity entity = invitationConverter.toEntity(invitation, orgId);
        InvitationEntity savedEntity = invitationRepository.save(entity);

        Invitation savedInvitation = invitationConverter.toDomain(savedEntity);

        mailInviteMessageSender.send(savedInvitation);

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

    @Transactional
    public void revoke(String orgId, String invitationId, InlineObject1 inlineObject) {
        InvitationEntity invitation = invitationRepository.findByIdAndOrganizationId(invitationId, orgId)
                .orElseThrow(ResourceNotFoundException::new);
        invitation.setStatus(inlineObject.getStatus().getValue());
        invitation.setRevocationReason(inlineObject.getReason());
        invitation.setRevokedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

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

    public InvitationEntity findByToken(String token) {
        InvitationEntity invitationEntity = invitationRepository.findByAcceptToken(token)
                .orElseThrow(ResourceNotFoundException::new);
        validateInvitation(invitationEntity);
        return invitationEntity;
    }

    private void validateInvitation(InvitationEntity invitationEntity) {
        if (invitationEntity.isExpired()) {
            throw new InviteExpiredException(invitationEntity.getExpiresAt().toString());
        }
        if (invitationEntity.getStatus().equalsIgnoreCase(InvitationStatusName.REVOKED.getValue())) {
            throw new InviteRevokedException(invitationEntity.getRevocationReason());
        }
    }

}
