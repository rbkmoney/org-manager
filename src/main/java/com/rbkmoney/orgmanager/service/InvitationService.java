package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.converter.InvitationConverter;
import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.repository.InvitationRepository;
import com.rbkmoney.orgmanager.repository.OrganizationRepository;
import com.rbkmoney.swag.organizations.model.InlineObject;
import com.rbkmoney.swag.organizations.model.InlineResponse2002;
import com.rbkmoney.swag.organizations.model.Invitation;
import com.rbkmoney.swag.organizations.model.InvitationStatusName;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationConverter invitationConverter;
    private final InvitationRepository invitationRepository;
    private final OrganizationRepository organizationRepository;

    // TODO [a.romanov]: idempotency
    public ResponseEntity<Invitation> create(
            String orgId,
            Invitation invitation,
            String xIdempotencyKey) {
        InvitationEntity entity = invitationConverter.toEntity(invitation, orgId);
        InvitationEntity savedEntity = invitationRepository.save(entity);

        Invitation savedInvitation = invitationConverter.toDomain(savedEntity);
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

    public ResponseEntity<InlineResponse2002> list(String orgId, InvitationStatusName status) {
        boolean isOrganizationExist = organizationRepository.existsById(orgId);

        if (!isOrganizationExist) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        List<InvitationEntity> entities = invitationRepository.findByOrganizationIdAndStatus(orgId, status.getValue());
        List<Invitation> invitations = entities.stream()
                .map(invitationConverter::toDomain)
                .collect(toList());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new InlineResponse2002()
                        .results(invitations));
    }

    public ResponseEntity<Void> revoke(String orgId, String invitationId, InlineObject inlineObject) {
        Optional<InvitationEntity> entity = invitationRepository.findById(invitationId);

        if (entity.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        InvitationEntity updatedEntity = entity.get();
        updatedEntity.setStatus(inlineObject.getStatus().getValue());
        updatedEntity.setRevocationReason(inlineObject.getReason());

        invitationRepository.save(updatedEntity);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
