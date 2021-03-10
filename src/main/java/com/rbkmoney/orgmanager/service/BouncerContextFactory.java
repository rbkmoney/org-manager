package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.context.v1.Auth;
import com.rbkmoney.bouncer.context.v1.ContextFragment;
import com.rbkmoney.bouncer.context.v1.ContextOrgManagement;
import com.rbkmoney.bouncer.context.v1.Deployment;
import com.rbkmoney.bouncer.context.v1.Entity;
import com.rbkmoney.bouncer.context.v1.Environment;
import com.rbkmoney.bouncer.context.v1.Invitee;
import com.rbkmoney.bouncer.context.v1.OrgManagementInvitation;
import com.rbkmoney.bouncer.context.v1.OrgManagementOperation;
import com.rbkmoney.bouncer.context.v1.OrgRole;
import com.rbkmoney.bouncer.context.v1.OrgRoleScope;
import com.rbkmoney.bouncer.context.v1.Token;
import com.rbkmoney.bouncer.context.v1.User;
import com.rbkmoney.bouncer.ctx.ContextFragmentType;
import com.rbkmoney.bouncer.decisions.Context;
import com.rbkmoney.orgmanagement.UserNotFound;
import com.rbkmoney.orgmanager.config.properties.BouncerProperties;
import com.rbkmoney.orgmanager.service.dto.BouncerContextDto;
import com.rbkmoney.orgmanager.service.dto.InvitationDto;
import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.keycloak.representations.AccessToken;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BouncerContextFactory {

    private final BouncerProperties bouncerProperties;
    private final UserService userService;
    private final KeycloakService keycloakService;

    public Context buildContext(BouncerContextDto bouncerContext) throws TException {
        Context context = new Context();
        com.rbkmoney.bouncer.ctx.ContextFragment fragment = new com.rbkmoney.bouncer.ctx.ContextFragment();
        fragment.setType(ContextFragmentType.v1_thrift_binary);
        ContextFragment contextFragment = buildContextFragment(bouncerContext);
        TSerializer serializer = new TSerializer();
        fragment.setContent(serializer.serialize(contextFragment));
        context.putToFragments(bouncerProperties.getContextFragmentId(), fragment);
        return context;
    }

    private ContextFragment buildContextFragment(BouncerContextDto bouncerContext) throws TException {
        Environment env = buildEnvironment();
        AccessToken accessToken = keycloakService.getAccessToken();
        User user = userService.findById(accessToken.getSubject());
        user.setRealm(new Entity().setId(bouncerProperties.getRealm()));
        String expiration = Instant.ofEpochSecond(accessToken.getExp()).toString();
        Auth auth = new Auth()
                .setToken(new Token().setId(accessToken.getId()))
                .setMethod(bouncerProperties.getAuthMethod())
                .setExpiration(expiration);
        // TODO надо ли доставать requester?
        ContextOrgManagement contextOrgManagement = buildOrgManagementContext(bouncerContext);
        return new ContextFragment()
                .setAuth(auth)
                .setUser(user)
                .setEnv(env)
                .setOrgmgmt(contextOrgManagement);

    }

    private Environment buildEnvironment() {
        Environment env = new Environment();
        Deployment deployment = new Deployment();
        deployment.setId(bouncerProperties.getDeploymentId());
        env.setDeployment(deployment)
                .setNow(Instant.now().toString());
        return env;
    }

    private ContextOrgManagement buildOrgManagementContext(BouncerContextDto bouncerContext) throws TException {
        ContextOrgManagement contextOrgManagement = new ContextOrgManagement();
        OrgManagementOperation orgManagementOperation =
                buildOrgManagementOperation(bouncerContext);
        contextOrgManagement.setOp(orgManagementOperation);
        OrgManagementInvitation orgManagementInvitation = buildOrgManagementInvitation(bouncerContext);
        contextOrgManagement.setInvitation(orgManagementInvitation);
        return contextOrgManagement;
    }

    private OrgManagementOperation buildOrgManagementOperation(BouncerContextDto bouncerContext) throws UserNotFound {
        OrgManagementOperation orgManagementOperation = new OrgManagementOperation();
        orgManagementOperation.setId(bouncerContext.getOperationName());
        if (Objects.nonNull(bouncerContext.getOrganizationId())) {
            orgManagementOperation.setOrganization(new Entity().setId(bouncerContext.getOrganizationId()));
        }
        if (Objects.nonNull(bouncerContext.getMemberId())) {
            User member = userService.findById(bouncerContext.getMemberId());
            orgManagementOperation.setMember(member);
        }
        if (Objects.nonNull(bouncerContext.getRole())) {
            OrgRole role = new OrgRole();
            role.setId(bouncerContext.getRole().getRoleId());
            OrgRoleScope orgRoleScope = new OrgRoleScope();
            orgRoleScope.setShop(new Entity().setId(bouncerContext.getRole().getScopeResourceId()));
            role.setScope(orgRoleScope);
        }
        return orgManagementOperation;
    }

    private OrgManagementInvitation buildOrgManagementInvitation(BouncerContextDto bouncerContext) {
        OrgManagementInvitation orgManagementInvitation = new OrgManagementInvitation();
        if (Objects.nonNull(bouncerContext.getOrganizationId())) {
            orgManagementInvitation.setOrganization(new Entity().setId(bouncerContext.getOrganizationId()));
        }
        if (Objects.nonNull(bouncerContext.getInvitation())) {
            InvitationDto invitationDto = bouncerContext.getInvitation();
            Invitee invitee = new Invitee();
            invitee.setEmail(invitationDto.getEmail());
            // TODO set invitationId
        }
        return orgManagementInvitation;
    }
}
