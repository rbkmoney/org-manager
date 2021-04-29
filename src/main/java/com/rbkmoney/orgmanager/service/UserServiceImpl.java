package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.context.v1.Organization;
import com.rbkmoney.bouncer.context.v1.User;
import com.rbkmoney.orgmanager.converter.BouncerContextConverter;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final MemberService memberService;
    private final BouncerContextConverter bouncerContextConverter;
    private final KeycloakService keycloakService;
    private final OrganizationService organizationService;

    @Transactional(readOnly = true)
    @Override
    public User findById(String id) {
        log.info("Find user with id {}", id);
        return memberService.findById(id)
                .map(bouncerContextConverter::toUser)
                .orElseGet(this::buildUser);
    }

    private User buildUser() {
        AccessToken accessToken = keycloakService.getAccessToken();
        Set<OrganizationEntity> organizationEntities = organizationService.findByOwner(accessToken.getSubject());
        Set<Organization> organizations = organizationEntities.stream()
                .map(organizationEntity -> bouncerContextConverter.toOrganization(organizationEntity, null))
                .collect(Collectors.toSet());
        return new User()
                .setId(accessToken.getSubject())
                .setEmail(accessToken.getEmail())
                .setOrgs(organizations);
    }
}
