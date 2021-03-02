package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.decisions.ArbiterSrv;
import com.rbkmoney.bouncer.decisions.Context;
import com.rbkmoney.bouncer.decisions.Judgement;
import com.rbkmoney.bouncer.decisions.Resolution;
import com.rbkmoney.bouncer.decisions.ResolutionAllowed;
import com.rbkmoney.bouncer.decisions.ResolutionRestricted;
import com.rbkmoney.bouncer.decisions.RulesetNotFound;
import com.rbkmoney.orgmanagement.UserNotFound;
import com.rbkmoney.orgmanager.TestObjectFactory;
import com.rbkmoney.orgmanager.config.properties.BouncerProperties;
import com.rbkmoney.orgmanager.service.dto.BouncerContextDto;
import org.apache.thrift.TException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class BouncerServiceImplTest {

    @Mock
    private ArbiterSrv.Iface bouncerClient;

    @Mock
    private BouncerContextFactory bouncerContextFactory;

    private BouncerProperties bouncerProperties;

    private BouncerService bouncerService;


    @BeforeEach
    void setUp() {
        bouncerProperties = new BouncerProperties();
        bouncerProperties.setEnabled(Boolean.TRUE);
        bouncerService = new BouncerServiceImpl(bouncerContextFactory, bouncerClient, bouncerProperties);
    }

    @Test
    void checkPrivilegesWithNotEnabledBouncer() {
        bouncerProperties.setEnabled(Boolean.FALSE);
        BouncerContextDto bouncerContext = TestObjectFactory.testBouncerContextDto();

        boolean result = bouncerService.checkPrivileges(bouncerContext);

        assertTrue(result);
    }

    @Test
    void checkPrivilegesWithIncorrectBuildContext() throws TException {
        BouncerContextDto bouncerContext = TestObjectFactory.testBouncerContextDto();
        when(bouncerContextFactory.buildContext(bouncerContext)).thenThrow(new UserNotFound());

        boolean result = bouncerService.checkPrivileges(bouncerContext);

        assertFalse(result);
    }

    @Test
    void checkPrivilegesWithIncorrectBouncerCall() throws TException {
        BouncerContextDto bouncerContext = TestObjectFactory.testBouncerContextDto();
        when(bouncerContextFactory.buildContext(bouncerContext)).thenReturn(new Context());
        when(bouncerClient.judge(anyString(), any(Context.class))).thenThrow(new RulesetNotFound());

        boolean result = bouncerService.checkPrivileges(bouncerContext);

        assertFalse(result);
    }

    @Test
    void checkPrivilegesWithRestrictedResolution() throws TException {
        BouncerContextDto bouncerContext = TestObjectFactory.testBouncerContextDto();
        when(bouncerContextFactory.buildContext(bouncerContext)).thenReturn(new Context());
        Judgement judgement = new Judgement();
        Resolution resolution = new Resolution();
        resolution.setRestricted(new ResolutionRestricted());
        judgement.setResolution(resolution);
        when(bouncerClient.judge(anyString(), any(Context.class))).thenReturn(judgement);

        boolean result = bouncerService.checkPrivileges(bouncerContext);

        assertFalse(result);
    }

    @Test
    void checkPrivilegesWithAllowedResolution() throws TException {
        BouncerContextDto bouncerContext = TestObjectFactory.testBouncerContextDto();
        when(bouncerContextFactory.buildContext(bouncerContext)).thenReturn(new Context());
        Judgement judgement = new Judgement();
        Resolution resolution = new Resolution();
        resolution.setAllowed(new ResolutionAllowed());
        judgement.setResolution(resolution);
        when(bouncerClient.judge(anyString(), any(Context.class))).thenReturn(judgement);

        boolean result = bouncerService.checkPrivileges(bouncerContext);

        assertTrue(result);
    }
}