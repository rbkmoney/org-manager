package com.rbkmoney.orgmanager.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.orgmanager.config.properties.InviteTokenProperties;
import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.orgmanager.util.JsonMapper;
import com.rbkmoney.swag.organizations.model.*;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
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
        InviteTokenProperties inviteTokenProperties = mock(InviteTokenProperties.class);
        when(inviteTokenProperties.getLifeTimeInDays()).thenReturn(30L);

        converter = new InvitationConverter(
                new JsonMapper(new ObjectMapper()),
                memberRoleConverter,
              inviteTokenProperties
              );
    }

    @Test
    public void shouldConvertToEntity() {
        // Given
        InvitationRequest invitation = new InvitationRequest()
                .invitee(new Invitee()
                        .contact(new InviteeContact()
                                .type(InviteeContact.TypeEnum.EMAIL)
                                .email("email"))
                        .roles(List.of(new MemberRole())))
                .metadata(Map.of("a", "b"));

        // When
        InvitationEntity entity = converter.toEntity(invitation, "org");

        // Then
        InvitationEntity expected = InvitationEntity.builder()
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
                        .roles(List.of(new MemberRole())))
                .acceptToken("token")
                .metadata(Map.of("a", "b"));
        expected.setStatus(InvitationStatusName.PENDING);

        assertThat(invitation).isEqualToComparingFieldByField(expected);
    }
}
