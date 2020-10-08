package com.rbkmoney.orgmanager.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.orgmanager.util.JsonMapper;
import com.rbkmoney.swag.organizations.model.*;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InvitationConverterTest {

    private InvitationConverter converter;

    @Before
    public void setUp() {
        MemberRoleConverter memberRoleConverter = mock(MemberRoleConverter.class);
        when(memberRoleConverter.toDomain(any(MemberRoleEntity.class)))
                .thenReturn(new MemberRole());
        when(memberRoleConverter.toEntity(any(MemberRole.class), anyString()))
                .thenReturn(new MemberRoleEntity());

        converter = new InvitationConverter(
                new JsonMapper(
                        new ObjectMapper()),
                memberRoleConverter);
    }

    @Test
    public void shouldConvertToEntity() {
        // Given
        Invitation invitation = new Invitation()
                .invitee(new Invitee()
                        .contact(new InviteeContact()
                                .type(InviteeContact.TypeEnum.EMAIL)
                                .email("email"))
                        .roles(Set.of(new MemberRole())))
                .expiresAt(OffsetDateTime.parse("2019-08-24T14:15:22Z"))
                .metadata(Map.of("a", "b"));
        invitation.setStatus("Pending");

        // When
        InvitationEntity entity = converter.toEntity(invitation, "org");

        // Then
        InvitationEntity expected = InvitationEntity.builder()
                .expiresAt(LocalDateTime.parse("2019-08-24T14:15:22"))
                .inviteeContactEmail("email")
                .inviteeContactType("EMail")
                .organizationId("org")
                .status("Pending")
                .metadata("{\"a\":\"b\"}")
                .build();

        assertThat(entity.getId()).isNotEmpty();
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getAcceptToken()).isNotNull();
        assertThat(entity.getInviteeRoles()).hasSize(1);
        assertThat(entity).isEqualToIgnoringNullFields(expected);
    }

    @Test
    public void shouldConvertToDomain() {
        // Given
        InvitationEntity entity = InvitationEntity.builder()
                .id("id")
                .expiresAt(LocalDateTime.parse("2019-08-24T14:15:22"))
                .createdAt(LocalDateTime.parse("2019-08-24T14:15:22"))
                .inviteeContactEmail("email")
                .inviteeContactType("EMail")
                .inviteeRoles(Set.of(new MemberRoleEntity()))
                .organizationId("org")
                .status("Pending")
                .acceptToken("token")
                .metadata("{\"a\":\"b\"}")
                .build();

        // When
        Invitation invitation = converter.toDomain(entity);

        // Then
        Invitation expected = new Invitation()
                .id("id")
                .expiresAt(OffsetDateTime.parse("2019-08-24T14:15:22Z"))
                .createdAt(OffsetDateTime.parse("2019-08-24T14:15:22Z"))
                .invitee(new Invitee()
                        .contact(new InviteeContact()
                                .type(InviteeContact.TypeEnum.EMAIL)
                                .email("email"))
                        .roles(Set.of(new MemberRole())))
                .acceptToken("token")
                .metadata(Map.of("a", "b"));
        expected.setStatus("Pending");

        assertThat(invitation).isEqualToComparingFieldByField(expected);
    }
}