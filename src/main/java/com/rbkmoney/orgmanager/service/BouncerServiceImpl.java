package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.decisions.ArbiterSrv;
import com.rbkmoney.bouncer.decisions.Context;
import com.rbkmoney.bouncer.decisions.Judgement;
import com.rbkmoney.bouncer.decisions.Resolution;
import com.rbkmoney.orgmanager.config.properties.BouncerProperties;
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
    public boolean checkPrivileges(BouncerContextDto bouncerContext) {
        if (!bouncerProperties.getEnabled()) {
            return true;
        }
        return passJudgement(bouncerContext);
    }

    private boolean passJudgement(BouncerContextDto bouncerContext) {
        try {
            Context context = bouncerContextFactory.buildContext(bouncerContext);
            // TODO откуда брать ruleSetId ?
            Judgement judge = bouncerClient.judge("ruleSetId", context);
            Resolution resolution = judge.getResolution();
            return resolution.isSetAllowed();
        } catch (TException e) {
            // TODO что делаем при исключении?
            return false;
        }
    }
}
