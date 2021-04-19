package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.decisions.ArbiterSrv;
import com.rbkmoney.bouncer.decisions.Context;
import com.rbkmoney.bouncer.decisions.Judgement;
import com.rbkmoney.bouncer.decisions.Resolution;
import com.rbkmoney.orgmanagement.UserNotFound;
import com.rbkmoney.orgmanager.config.properties.BouncerProperties;
import com.rbkmoney.orgmanager.exception.BouncerException;
import com.rbkmoney.orgmanager.service.dto.BouncerContextDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BouncerServiceImpl implements BouncerService {

    private final BouncerContextFactory bouncerContextFactory;
    private final ArbiterSrv.Iface bouncerClient;
    private final BouncerProperties bouncerProperties;

    @Override
    public boolean havePrivileges(BouncerContextDto bouncerContext) {
        try {
            Context context = bouncerContextFactory.buildContext(bouncerContext);
            Judgement judge = bouncerClient.judge(bouncerProperties.getRuleSetId(), context);
            Resolution resolution = judge.getResolution();
            return resolution.isSetAllowed();
        } catch (UserNotFound e) {
            log.error("Error while build bouncer context", e);
            throw new BouncerException();
        } catch (TException e) {
            log.error("Error while call bouncer", e);
            throw new BouncerException();
        }
    }
}
