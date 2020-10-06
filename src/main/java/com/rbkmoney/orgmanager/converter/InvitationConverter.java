package com.rbkmoney.orgmanager.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.util.JsonMapper;
import com.rbkmoney.swag.organizations.model.Invitation;
import com.rbkmoney.swag.organizations.model.Invitee;
import com.rbkmoney.swag.organizations.model.InviteeContact;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class InvitationConverter {

    private final JsonMapper jsonMapper;
    private final RoleConverter roleConverter;

    @SneakyThrows(JsonProcessingException.class)
    public InvitationEntity toEntity(Invitation invitation, String orgId) {
        Optional<Invitee> invitee = Optional.ofNullable(invitation.getInvitee());

        return InvitationEntity.builder()
                .id(UUID.randomUUID().toString())
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
                .inviteeRoles(invitee
                        .map(Invitee::getRoles)
                        .orElse(emptySet())
                        .stream()
                        .map(role -> roleConverter.toEntity(role, orgId))
                        .collect(toSet()))
                .metadata(jsonMapper.toJson(invitation.getMetadata()))
//                .status(Optional.ofNullable(invitation.getStatus()).orElse("Pending")) // TODO [a.romanov]: swag fix
                .acceptToken(invitation.getAcceptToken())
                .build();
    }

    public Invitation toDomain(InvitationEntity entity) {
        Invitation invitation = new Invitation()
                .id(entity.getId())
                .createdAt(OffsetDateTime.of(entity.getCreatedAt(), ZoneOffset.UTC))
                .expiresAt(OffsetDateTime.of(entity.getExpiresAt(), ZoneOffset.UTC))
                .acceptToken(entity.getAcceptToken())
                .metadata(jsonMapper.toMap(entity.getMetadata()))
                .invitee(new Invitee()
                        .contact(new InviteeContact()
                                .type(InviteeContact.TypeEnum.fromValue(entity.getInviteeContactType()))
                                .email(entity.getInviteeContactEmail()))
                        .roles(entity.getInviteeRoles()
                                .stream()
                                .map(roleConverter::toDomain)
                                .collect(toSet())));
//        invitation.setStatus(entity.getStatus()); // TODO [a.romanov]: swag fix

        return invitation;
    }
}
