package com.rbkmoney.orgmanager.repository;

import com.rbkmoney.orgmanager.OrgManagerApplication;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Duration;

@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {OrgManagerApplication.class})
@ContextConfiguration(initializers = InvitationRepositoryTest.Initializer.class)
public abstract class AbstractRepositoryTest {

    @Autowired
    protected InvitationRepository invitationRepository;

    @Autowired
    protected OrganizationRepository organizationRepository;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected MemberRoleRepository memberRoleRepository;

    @Autowired
    protected OrganizationRoleRepository organizationRoleRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    public void setUp() throws Exception {
        withTransaction(() -> {
            invitationRepository.deleteAll();
            organizationRepository.deleteAll();
            memberRepository.deleteAll();
            memberRoleRepository.deleteAll();
            organizationRoleRepository.deleteAll();
        });
    }

    @ClassRule
    @SuppressWarnings("rawtypes")
    public static PostgreSQLContainer postgres = new PostgreSQLContainer<>("postgres:9.6")
            .withStartupTimeout(Duration.ofMinutes(5));

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            postgres.start();
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

    protected void withTransaction(Runnable runnable) {
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
