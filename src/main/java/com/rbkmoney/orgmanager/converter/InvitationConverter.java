package com.rbkmoney.orgmanager.converter;

import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.util.JsonMapper;
import com.rbkmoney.swag.organizations.model.Invitation;
import com.rbkmoney.swag.organizations.model.Invitee;
import com.rbkmoney.swag.organizations.model.InviteeContact;
import lombok.RequiredArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;
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
    private final MemberRoleConverter memberRoleConverter;

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
                        .orElse(""))
                .inviteeRoles(invitee
                        .map(Invitee::getRoles)
                        .orElse(emptySet())
                        .stream()
                        .map(role -> memberRoleConverter.toEntity(role, orgId))
                        .collect(toSet()))
                .metadata(jsonMapper.toJson(invitation.getMetadata()))
                .status(invitation.getStatus().get().toString())
                .acceptToken(UUID.randomUUID().toString()) // TODO [a.romanov]: token
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
                                .map(memberRoleConverter::toDomain)
                                .collect(toSet())));
        invitation.setStatus(JsonNullable.of(entity.getStatus()));

        return invitation;
    }
}
