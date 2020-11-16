package com.rbkmoney.orgmanager.converter;

import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.swag.organizations.model.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MemberConverterTest {

    private MemberConverter converter;

    @Before
    public void setUp() {
        MemberRoleConverter memberRoleConverter = mock(MemberRoleConverter.class);
        when(memberRoleConverter.toDomain(any(MemberRoleEntity.class)))
                .thenReturn(new MemberRole());
        when(memberRoleConverter.toEntity(any(MemberRole.class), anyString()))
                .thenReturn(new MemberRoleEntity());

        converter = new MemberConverter(memberRoleConverter);
    }

    @Test
    public void shouldConvertToDomain() {
        // Given
        MemberEntity entity = MemberEntity.builder()
                .id("id")
                .email("email")
                .roles(Set.of(new MemberRoleEntity()))
                .build();

        // When
        Member member = converter.toDomain(entity);

        // Then
        Member expected = new Member()
                .id("id")
                .userEmail("email")
                .roles(Set.of(new MemberRole()));

        assertThat(member).isEqualToComparingFieldByField(expected);
    }
}