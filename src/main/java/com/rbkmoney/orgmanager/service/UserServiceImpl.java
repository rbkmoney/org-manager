package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.context.v1.Organization;
import com.rbkmoney.bouncer.context.v1.User;
import com.rbkmoney.orgmanager.converter.BouncerContextConverter;
import com.rbkmoney.orgmanager.entity.MemberEntity;
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
        AccessToken accessToken = keycloakService.getAccessToken();
        Set<Organization> ownedOrganizations = findOwnedOrganizations(id);
        return memberService.findById(id)
                .map(memberEntity -> buildMemberUser(ownedOrganizations, memberEntity))
                .orElseGet(() ->
                        new User()
                                .setId(id)
                                .setEmail(accessToken.getEmail())
                                .setOrgs(ownedOrganizations));
    }

    private Set<Organization> findOwnedOrganizations(String owner) {
        Set<OrganizationEntity> organizationEntities = organizationService.findByOwner(owner);
        return organizationEntities.stream()
                .map(organizationEntity -> bouncerContextConverter.toOrganization(organizationEntity, null))
                .collect(Collectors.toSet());
    }

    private User buildMemberUser(Set<Organization> organizations, MemberEntity memberEntity) {
        User user = bouncerContextConverter.toUser(memberEntity);
        user.getOrgs().addAll(organizations);
        return user;
    }
}
