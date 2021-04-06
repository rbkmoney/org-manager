package com.rbkmoney.orgmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.orgmanager.repository.AbstractRepositoryTest;
import com.rbkmoney.orgmanager.service.ResourceAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@Import(KeycloakTestConfig.class)
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@TestPropertySource(locations = "classpath:wiremock.properties")
public abstract class AbstractControllerTest extends AbstractRepositoryTest {

    @SpyBean
    protected ResourceAccessService resourceAccessService;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        keycloakOpenIdStub.givenStub();
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

}
