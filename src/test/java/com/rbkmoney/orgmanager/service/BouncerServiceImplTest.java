package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.decisions.*;
import com.rbkmoney.orgmanagement.UserNotFound;
import com.rbkmoney.orgmanager.TestObjectFactory;
import com.rbkmoney.orgmanager.config.properties.BouncerProperties;
import com.rbkmoney.orgmanager.exception.BouncerException;
import com.rbkmoney.orgmanager.service.dto.BouncerContextDto;
import org.apache.thrift.TException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class BouncerServiceImplTest {

    @Mock
    private ArbiterSrv.Iface bouncerClient;

    @Mock
    private BouncerContextFactory bouncerContextFactory;

    private BouncerService bouncerService;


    @BeforeEach
    void setUp() {
        BouncerProperties bouncerProperties = new BouncerProperties();
        bouncerProperties.setRuleSetId(TestObjectFactory.randomString());
        bouncerService = new BouncerServiceImpl(bouncerContextFactory, bouncerClient, bouncerProperties);
    }

    @Test
    void havePrivilegesWithIncorrectBuildContext() throws TException {
        BouncerContextDto bouncerContext = TestObjectFactory.testBouncerContextDto();
        when(bouncerContextFactory.buildContext(bouncerContext)).thenThrow(new UserNotFound());

        var exception = assertThrows(BouncerException.class, () -> bouncerService.havePrivileges(bouncerContext));

    }

    @Test
    void havePrivilegesWithIncorrectBouncerCall() throws TException {
        BouncerContextDto bouncerContext = TestObjectFactory.testBouncerContextDto();
        when(bouncerContextFactory.buildContext(bouncerContext)).thenReturn(new Context());
        when(bouncerClient.judge(anyString(), any(Context.class))).thenThrow(new RulesetNotFound());

        var exception = assertThrows(BouncerException.class, () -> bouncerService.havePrivileges(bouncerContext));
    }

    @Test
    void havePrivilegesWithRestrictedResolution() throws TException {
        BouncerContextDto bouncerContext = TestObjectFactory.testBouncerContextDto();
        when(bouncerContextFactory.buildContext(bouncerContext)).thenReturn(new Context());
        Judgement judgement = new Judgement();
        Resolution resolution = new Resolution();
        resolution.setRestricted(new ResolutionRestricted());
        judgement.setResolution(resolution);
        when(bouncerClient.judge(anyString(), any(Context.class))).thenReturn(judgement);

        boolean result = bouncerService.havePrivileges(bouncerContext);

        assertFalse(result);
    }

    @Test
    void havePrivilegesWithAllowedResolution() throws TException {
        BouncerContextDto bouncerContext = TestObjectFactory.testBouncerContextDto();
        when(bouncerContextFactory.buildContext(bouncerContext)).thenReturn(new Context());
        Judgement judgement = new Judgement();
        Resolution resolution = new Resolution();
        resolution.setAllowed(new ResolutionAllowed());
        judgement.setResolution(resolution);
        when(bouncerClient.judge(anyString(), any(Context.class))).thenReturn(judgement);

        boolean result = bouncerService.havePrivileges(bouncerContext);

        assertTrue(result);
    }
}