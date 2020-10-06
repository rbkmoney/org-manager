package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.converter.InvitationConverter;
import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.repository.InvitationRepository;
import com.rbkmoney.swag.organizations.model.InlineResponse2003;
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

    // TODO [a.romanov]: idempotency
    public ResponseEntity<Invitation> create(
            String orgId,
            Invitation invitation,
            String xIdempotencyKey) {
        InvitationEntity entity = invitationConverter.toEntity(invitation, orgId);
        invitationRepository.save(entity);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(invitation);
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

    public ResponseEntity<InlineResponse2003> list(String orgId, InvitationStatusName status) {
        List<InvitationEntity> entities = invitationRepository.findByOrganizationIdAndStatus(orgId, status.getValue());
        List<Invitation> invitations = entities.stream()
                .map(invitationConverter::toDomain)
                .collect(toList());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new InlineResponse2003()
                        .results(invitations));
    }
}
