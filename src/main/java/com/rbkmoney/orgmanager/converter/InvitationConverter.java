package com.rbkmoney.orgmanager.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.swag.organizations.model.Invitation;
import com.rbkmoney.swag.organizations.model.Invitee;
import com.rbkmoney.swag.organizations.model.InviteeContact;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvitationConverter {

    private final ObjectMapper objectMapper;

    @SneakyThrows(JsonProcessingException.class)
    public InvitationEntity toEntity(Invitation invitation, String orgId) {
        Optional<Invitee> invitee = Optional.ofNullable(invitation.getInvitee());

        return InvitationEntity.builder()
                .id(invitation.getId()) // TODO [a.romanov]: generated?
                .organizationId(orgId)
                .createdAt(LocalDateTime.now())
                .expiresAt(invitation.getExpiresAt().toLocalDateTime())
                .inviteeContactType(invitee
                        .map(Invitee::getContact)
                        .map(InviteeContact::getType)
                        .map(InviteeContact.TypeEnum::getValue)
                        .orElse(null))
                .inviteeContactEmail(invitee
                        .map(Invitee::getContact)
                        .map(InviteeContact::getEmail)
                        .orElse(null))
//                .inviteeRoles() // TODO [a.romanov]: convert roles
                .metadata(objectMapper.writeValueAsString(invitation.getMetadata()))
                .status(Optional.ofNullable(invitation.getStatus()).orElse("PENDING")) // TODO [a.romanov]: swag fix
                .acceptToken()
                .build();
    }

    @SneakyThrows(JsonProcessingException.class)
    public Invitation toDomain(InvitationEntity entity) {
        Invitation invitation = new Invitation()
                .id(entity.getId())
                .createdAt(OffsetDateTime.of(entity.getCreatedAt(), ZoneOffset.UTC))
                .expiresAt(OffsetDateTime.of(entity.getExpiresAt(), ZoneOffset.UTC))
                .acceptToken(entity.getAcceptToken())
                .metadata(objectMapper.readValue(entity.getMetadata(), Map.class))
                .invitee(new Invitee()
                                .contact(new InviteeContact()
                                        .type(InviteeContact.TypeEnum.fromValue(entity.getInviteeContactType()))
                                        .email(entity.getInviteeContactEmail()))
//                        .roles() // TODO [a.romanov]: convert roles
                );
        invitation.setStatus(entity.getStatus()); // TODO [a.romanov]: swag fix

        return invitation;
    }
}
