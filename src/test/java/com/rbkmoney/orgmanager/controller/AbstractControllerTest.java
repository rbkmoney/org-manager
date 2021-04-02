package com.rbkmoney.orgmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.orgmanager.repository.InvitationRepository;
import com.rbkmoney.orgmanager.repository.MemberRepository;
import com.rbkmoney.orgmanager.repository.MemberRoleRepository;
import com.rbkmoney.orgmanager.repository.OrganizationRepository;
import com.rbkmoney.orgmanager.service.ResourceAccessService;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Duration;

@Import(KeycloakTestConfig.class)
public abstract class AbstractControllerTest {

    @Autowired
    protected InvitationRepository invitationRepository;

    @Autowired
    protected OrganizationRepository organizationRepository;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected MemberRoleRepository memberRoleRepository;

    @SpyBean
    protected ResourceAccessService resourceAccessService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    public void setUp() throws Exception {
        keycloakOpenIdStub.givenStub();
        withTransaction(() -> {
            invitationRepository.deleteAll();
            organizationRepository.deleteAll();
            memberRepository.deleteAll();
            memberRoleRepository.deleteAll();
        });
    }

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
    protected KeycloakOpenIdStub keycloakOpenIdStub;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String generateJwt(long iat, long exp, String... roles) {
        return keycloakOpenIdStub.generateJwt(iat, exp, roles);
    }

    protected String generateRBKadminJwt() {
        return keycloakOpenIdStub.generateJwt("RBKadmin");
    }

    protected String getUserFromToken() {
        return keycloakOpenIdStub.getUserId();
    }

    protected void withTransaction(Runnable runnable){
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            runnable.run();
            return;
        }
        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                runnable.run();
            }
        });
    }

}
