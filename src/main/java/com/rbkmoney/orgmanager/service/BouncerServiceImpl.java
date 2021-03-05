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
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

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
            throw new BouncerException("Error while build bouncer context", e);
        } catch (TException e) {
            throw new BouncerException("Error while call bouncer", e);
        }
    }
}
