package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.ctx.ContextFragment;
import com.rbkmoney.bouncer.ctx.ContextFragmentType;
import com.rbkmoney.bouncer.decisions.Context;
import com.rbkmoney.orgmanager.TestObjectFactory;
import com.rbkmoney.orgmanager.config.properties.BouncerProperties;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BouncerContextFactoryTest {

    @Mock
    private UserService userService;

    @Mock
    private KeycloakService keycloakService;

    private BouncerProperties bouncerProperties;

    private BouncerContextFactory bouncerContextFactory;

    @BeforeEach
    void setUp() {
        bouncerProperties = new BouncerProperties();
        bouncerProperties.setContextFragmentId(TestObjectFactory.randomString());
        bouncerProperties.setAuthMethod(TestObjectFactory.randomString());
        bouncerProperties.setDeploymentId(TestObjectFactory.randomString());
        bouncerProperties.setRealm(TestObjectFactory.randomString());
        bouncerContextFactory = new BouncerContextFactory(bouncerProperties, userService, keycloakService);
    }


    @Test
    void buildContextSuccess() throws TException {
        var user = TestObjectFactory.testUser();
        var member = TestObjectFactory.testUser();
        var token = TestObjectFactory.testToken();
        var bouncerContext = TestObjectFactory.testBouncerContextDto();
        when(userService.findById(token.getSubject())).thenReturn(user);
        when(userService.findById(bouncerContext.getMemberId())).thenReturn(member);
        when(keycloakService.getAccessToken()).thenReturn(token);

        Context context = bouncerContextFactory.buildContext(bouncerContext);

        ContextFragment fragment = context.getFragments().get(bouncerProperties.getContextFragmentId());
        com.rbkmoney.bouncer.context.v1.ContextFragment contextFragment =
                new com.rbkmoney.bouncer.context.v1.ContextFragment();
        TDeserializer tDeserializer = new TDeserializer();
        tDeserializer.deserialize(contextFragment, fragment.getContent());

        assertEquals(ContextFragmentType.v1_thrift_binary, fragment.getType());
        assertEquals(token.getId(), contextFragment.getAuth().getToken().getId());
        assertEquals(user.getId(), contextFragment.getUser().getId());
        assertEquals(bouncerContext.getOperationName(), contextFragment.getOrgmgmt().getOp().getId());
        assertEquals(member.getId(), contextFragment.getOrgmgmt().getOp().getMember().getId());
        verify(userService, times(2)).findById(anyString());
    }

}