package com.rbkmoney.orgmanager.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.util.JsonMapper;
import com.rbkmoney.swag.organizations.model.Organization;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OrganizationConverterTest {

    private OrganizationConverter converter;

    @Before
    public void setUp() {
        converter = new OrganizationConverter(
                new JsonMapper(
                        new ObjectMapper()));
    }

    @Test
    public void shouldConvertToEntity() {
        // Given
        Organization organization = new Organization()
                .name("org")
                .owner("own")
                .metadata(Map.of("a", "b"));

        // When
        OrganizationEntity entity = converter.toEntity(organization);

        // Then
        OrganizationEntity expected = OrganizationEntity.builder()
                .name("org")
                .owner("own")
                .metadata("{\"a\":\"b\"}")
                .build();

        assertThat(entity.getId()).isNotEmpty();
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity).isEqualToIgnoringNullFields(expected);
    }

    @Test
    public void shouldConvertToDomain() {
        // Given
        OrganizationEntity entity = OrganizationEntity.builder()
                .id("id")
                .createdAt(LocalDateTime.parse("2019-08-24T14:15:22"))
                .name("org")
                .owner("own")
                .metadata("{\"a\":\"b\"}")
                .build();

        // When
        Organization organization = converter.toDomain(entity);

        // Then
        Organization expected = new Organization()
                .id("id")
                .createdAt(OffsetDateTime.parse("2019-08-24T14:15:22Z"))
                .name("org")
                .owner("own")
                .metadata(Map.of("a", "b"));

        assertThat(organization).isEqualToComparingFieldByField(expected);
    }
}