package com.rbkmoney.orgmanager.repository;

import com.rbkmoney.orgmanager.OrgManagerApplication;
import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.entity.RoleEntity;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

@DirtiesContext
@SpringBootTest(classes = OrgManagerApplication.class)
@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = InvitationRepositoryTest.Initializer.class)
public class InvitationRepositoryTest {

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private RoleRepository roleRepository;

    @ClassRule
    @SuppressWarnings("rawtypes")
    public static PostgreSQLContainer postgres = new PostgreSQLContainer<>("postgres:9.6")
            .withStartupTimeout(Duration.ofMinutes(5));

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgres.getJdbcUrl(),
                    "spring.datasource.username=" + postgres.getUsername(),
                    "spring.datasource.password=" + postgres.getPassword(),
                    "spring.flyway.url=" + postgres.getJdbcUrl(),
                    "spring.flyway.user=" + postgres.getUsername(),
                    "spring.flyway.password=" + postgres.getPassword())
                    .and(configurableApplicationContext.getEnvironment().getActiveProfiles())
                    .applyTo(configurableApplicationContext);
        }
    }

    @Test
    public void shouldName() {
        // Given
//        roleRepository.save(RoleEntity.builder()
//                .id("role-1-id")
//                .name("role-1")
//                .organizationId("orgId")
//                .build());
//
//        roleRepository.save(RoleEntity.builder()
//                .id("role-2-id")
//                .name("role-2")
//                .organizationId("orgId")
//                .build());

        invitationRepository.save(InvitationEntity.builder()
                .id("invId")
                .acceptToken("token")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now())
                .inviteeContactEmail("contact")
                .inviteeContactType("type")
                .metadata("meta")
                .organizationId("orgId")
                .inviteeRoles(Set.of(
                        RoleEntity.builder()
                                .id("role-1-id")
                                .name("role-1")
                                .organizationId("orgId")
                                .build(),
                        RoleEntity.builder()
                                .id("role-2-id")
                                .name("role-2")
                                .organizationId("orgId")
                                .build()))
                .build());

        // When
        System.out.println(invitationRepository.findByOrganizationId("orgId").get(0).getInviteeRoles());

        // Then
    }
}