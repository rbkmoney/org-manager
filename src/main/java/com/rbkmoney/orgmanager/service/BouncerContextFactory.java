package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.context.v1.Auth;
import com.rbkmoney.bouncer.context.v1.ContextFragment;
import com.rbkmoney.bouncer.context.v1.Deployment;
import com.rbkmoney.bouncer.context.v1.Environment;
import com.rbkmoney.bouncer.context.v1.Token;
import com.rbkmoney.bouncer.context.v1.User;
import com.rbkmoney.bouncer.ctx.ContextFragmentType;
import com.rbkmoney.bouncer.decisions.Context;
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

    public static final String CONTEXT_FRAGMENT_ID = "orgmgmt";
    private static final String PRODUCTION_ID = "production";
    private static final String METHOD = "SessionToken"; // TODO мб надо тянуть откуда-то
    private static final TSerializer serializer = new TSerializer();

    private final UserService userService;
    private final KeycloakService keycloakService;

    public Context buildContext() throws TException {
        Context context = new Context();
        com.rbkmoney.bouncer.ctx.ContextFragment fragment = new com.rbkmoney.bouncer.ctx.ContextFragment();
        fragment.setType(ContextFragmentType.v1_thrift_binary);
        ContextFragment contextFragment = buildContextFragment();
        fragment.setContent(serializer.serialize(contextFragment));
        context.putToFragments(CONTEXT_FRAGMENT_ID, fragment);
        return context;
    }

    private ContextFragment buildContextFragment() throws TException {
        Environment env = buildEnvironment();
        AccessToken accessToken = keycloakService.getAccessToken();
        User user = userService.findById(accessToken.getSubject());
        String expiration = ZonedDateTime
                .ofInstant(Instant.ofEpochSecond(accessToken.getExp()), ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);
        Auth auth = new Auth()
                .setToken(new Token().setId(accessToken.getId()))
                .setMethod(METHOD)
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
        deployment.setId(PRODUCTION_ID);
        env.setDeployment(deployment)
                .setNow(ZonedDateTime
                        .now()
                        .truncatedTo(ChronoUnit.SECONDS)
                        .format(DateTimeFormatter.ISO_INSTANT));
        return env;
    }
}
