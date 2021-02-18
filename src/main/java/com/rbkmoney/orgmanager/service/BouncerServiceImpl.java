package com.rbkmoney.orgmanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.bouncer.context.v1.Deployment;
import com.rbkmoney.bouncer.context.v1.Environment;
import com.rbkmoney.bouncer.ctx.ContextFragment;
import com.rbkmoney.bouncer.ctx.ContextFragmentType;
import com.rbkmoney.bouncer.decisions.ArbiterSrv;
import com.rbkmoney.bouncer.decisions.Context;
import com.rbkmoney.bouncer.decisions.Judgement;
import com.rbkmoney.bouncer.decisions.Resolution;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class BouncerServiceImpl implements BouncerService {

    private static final String CONTEXT_FRAGMENT_ID = "orgmgmt";
    private static final String PRODUCTION_ID = "production";

    private final ArbiterSrv.Iface arbiterService;
    private final ObjectMapper mapper;

    @Override
    public boolean verify() {
        Context context = buildContext();

        Judgement judgement = arbiterService.judge("", context);

        Resolution resolution = judgement.getResolution();

        return resolution.isSetAllowed();
    }

    private Context buildContext() throws JsonProcessingException {
        Context context = new Context();
        ContextFragment fragment = new ContextFragment();
        fragment.setType(ContextFragmentType.v1_thrift_binary);
        com.rbkmoney.bouncer.context.v1.ContextFragment contextFragment = buildContextFragment();
        fragment.setContent(mapper.writeValueAsBytes(contextFragment));
        context.putToFragments(CONTEXT_FRAGMENT_ID, fragment);
        return context;
    }

    private com.rbkmoney.bouncer.context.v1.ContextFragment buildContextFragment() {
        com.rbkmoney.bouncer.context.v1.ContextFragment contextFragment =
                new com.rbkmoney.bouncer.context.v1.ContextFragment();
        Environment env = new Environment();
        Deployment deployment = new Deployment();
        deployment.setId(PRODUCTION_ID);
        env.setDeployment(deployment)
                .setNow(LocalDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
        contextFragment.setEnv(env);
        return contextFragment;
    }
}
