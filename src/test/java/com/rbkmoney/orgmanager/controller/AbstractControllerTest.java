package com.rbkmoney.orgmanager.controller;

import org.junit.ClassRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Duration;

@DirtiesContext
@Import(KeycloakTestConfig.class)
public abstract class AbstractControllerTest {

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

    @Autowired
    private KeycloakOpenIdStub keycloakOpenIdStub;

    protected String generateJwt(long iat, long exp, String... roles) {
        return keycloakOpenIdStub.generateJwt(iat, exp, roles);
    }

    protected String generateRBKadminJwt() {
        return keycloakOpenIdStub.generateJwt("RBKadmin");
    }

    protected String getUserFromToken() {
        return keycloakOpenIdStub.getUserId();
    }

}
