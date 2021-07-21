package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.context.v1.*;
import com.rbkmoney.bouncer.ctx.ContextFragmentType;
import com.rbkmoney.bouncer.decisions.Context;
import com.rbkmoney.orgmanagement.UserNotFound;
import com.rbkmoney.orgmanager.config.properties.BouncerProperties;
import com.rbkmoney.orgmanager.converter.BouncerContextConverter;
import com.rbkmoney.orgmanager.service.dto.BouncerContextDto;
import com.rbkmoney.orgmanager.service.dto.InvitationDto;
import com.rbkmoney.orgmanager.service.model.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.keycloak.representations.AccessToken;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class BouncerContextFactory {

    private final BouncerContextConverter bouncerConverter;
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
        // TODO надо ли доставать requester?
        ContextFragment contextFragment = new ContextFragment()
                .setAuth(buildAuth())
                .setUser(buildUser())
                .setEnv(buildEnvironment())
                .setOrgmgmt(buildOrgManagementContext(bouncerContext));
        log.debug("Context fragment to bouncer {}", contextFragment);
        return contextFragment;

    }

    private Auth buildAuth() {
        AccessToken accessToken = keycloakService.getAccessToken();
        String expiration = Instant.ofEpochSecond(accessToken.getExp()).toString();
        return new Auth()
                .setToken(new Token().setId(accessToken.getId()))
                .setMethod(bouncerProperties.getAuthMethod())
                .setExpiration(expiration);
    }

    private User buildUser() {
        AccessToken accessToken = keycloakService.getAccessToken();
        UserInfo userInfo = userService.findById(accessToken.getSubject());
        User bouncerUser = bouncerConverter.toUser(userInfo.getMember(), userInfo.getOrganizations());
        if (userInfo.getMember() == null) {
            bouncerUser.setId(accessToken.getSubject());
            bouncerUser.setEmail(accessToken.getEmail());
        }
        bouncerUser.setRealm(new Entity().setId(bouncerProperties.getRealm()));
        return bouncerUser;
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
            orgManagementOperation.setMember(buildUser());
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
            orgManagementInvitation.setId(invitationDto.getInvitationId());
        }
        return orgManagementInvitation;
    }
}
