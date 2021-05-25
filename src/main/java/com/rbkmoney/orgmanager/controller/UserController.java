package com.rbkmoney.orgmanager.controller;

import com.rbkmoney.orgmanager.service.KeycloakService;
import com.rbkmoney.orgmanager.service.OrganizationService;
import com.rbkmoney.orgmanager.service.ResourceAccessService;
import com.rbkmoney.orgmanager.service.dto.ResourceDto;
import com.rbkmoney.swag.organizations.api.UserApi;
import com.rbkmoney.swag.organizations.model.OrganizationJoinRequest;
import com.rbkmoney.swag.organizations.model.OrganizationMembership;
import com.rbkmoney.swag.organizations.model.OrganizationSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.AccessToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final OrganizationService organizationService;
    private final KeycloakService keycloakService;
    private final ResourceAccessService resourceAccessService;

    @Override
    public ResponseEntity<Void> cancelOrgMembership(
            String requestId,
            String orgId) {
        log.info("Cancel org membership: orgId={}", orgId);
        ResourceDto resource = ResourceDto.builder()
                .orgId(orgId)
                .build();
        resourceAccessService.checkRights(resource);
        AccessToken accessToken = keycloakService.getAccessToken();
        return organizationService.cancelOrgMembership(orgId, accessToken.getSubject(), accessToken.getEmail());
    }

    @Override
    public ResponseEntity<OrganizationMembership> inquireOrgMembership(
            String requestId,
            String orgId) {
        log.info("Inquire org membership: orgId={}", orgId);
        ResourceDto resource = ResourceDto.builder()
                .orgId(orgId)
                .build();
        resourceAccessService.checkRights(resource);
        AccessToken accessToken = keycloakService.getAccessToken();
        return organizationService.getMembership(orgId, accessToken.getSubject(), accessToken.getEmail());
    }

    @Override
    public ResponseEntity<OrganizationMembership> joinOrg(
            String requestId,
            OrganizationJoinRequest body) {
        log.info("Join organization: body={}", body);
        ResourceDto resource = ResourceDto.builder()
                .invitationToken(body.getInvitation())
                .build();
        resourceAccessService.checkRights(resource);
        AccessToken accessToken = keycloakService.getAccessToken();
        return ResponseEntity.ok(organizationService
                .joinOrganization(body.getInvitation(), accessToken.getSubject(), accessToken.getEmail()));
    }

    @Override
    public ResponseEntity<OrganizationSearchResult> listOrgMembership(String requestId,
                                                                      Integer limit,
                                                                      String continuationToken) {
        log.info("List org membership: limit={}, continuationToken={}", limit, continuationToken);
        resourceAccessService.checkRights();
        AccessToken accessToken = keycloakService.getAccessToken();
        OrganizationSearchResult organizationSearchResult =
                organizationService.findAllOrganizations(accessToken.getSubject(), limit, continuationToken);
        return ResponseEntity.ok(organizationSearchResult);
    }
}
