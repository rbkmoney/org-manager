package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.context.v1.Auth;
import com.rbkmoney.bouncer.context.v1.ContextFragment;
import com.rbkmoney.bouncer.context.v1.Deployment;
import com.rbkmoney.bouncer.context.v1.Entity;
import com.rbkmoney.bouncer.context.v1.Environment;
import com.rbkmoney.bouncer.context.v1.Token;
import com.rbkmoney.bouncer.context.v1.User;
import com.rbkmoney.bouncer.ctx.ContextFragmentType;
import com.rbkmoney.bouncer.decisions.Context;
import com.rbkmoney.orgmanager.config.properties.BouncerProperties;
import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.keycloak.representations.AccessToken;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class BouncerContextFactory {

    private final BouncerProperties bouncerProperties;
    private final UserService userService;
    private final KeycloakService keycloakService;

    public Context buildContext() throws TException {
        Context context = new Context();
        com.rbkmoney.bouncer.ctx.ContextFragment fragment = new com.rbkmoney.bouncer.ctx.ContextFragment();
        fragment.setType(ContextFragmentType.v1_thrift_binary);
        ContextFragment contextFragment = buildContextFragment();
        TSerializer serializer = new TSerializer();
        fragment.setContent(serializer.serialize(contextFragment));
        context.putToFragments(bouncerProperties.getContextFragmentId(), fragment);
        return context;
    }

    private ContextFragment buildContextFragment() throws TException {
        Environment env = buildEnvironment();
        AccessToken accessToken = keycloakService.getAccessToken();
        User user = userService.findById(accessToken.getSubject());
        user.setRealm(new Entity().setId(bouncerProperties.getRealm()));
        String expiration = ZonedDateTime
                .ofInstant(Instant.ofEpochSecond(accessToken.getExp()), ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);
        Auth auth = new Auth()
                .setToken(new Token().setId(accessToken.getId()))
                .setMethod(bouncerProperties.getAuthMethod())
                .setExpiration(expiration);
        // TODO надо ли доставать requester?
        return new ContextFragment()
                .setAuth(auth)
                .setUser(user)
                .setEnv(env);
    }

    private Environment buildEnvironment() {
        Environment env = new Environment();
        Deployment deployment = new Deployment();
        deployment.setId(bouncerProperties.getDeploymentId());
        env.setDeployment(deployment)
                .setNow(ZonedDateTime
                        .now()
                        .truncatedTo(ChronoUnit.SECONDS)
                        .format(DateTimeFormatter.ISO_INSTANT));
        return env;
    }

    public static void main(String[] args) {
        System.out.println(Instant.now().toString());
    }
}
