package com.rbkmoney.orgmanager.controller;

import com.rbkmoney.orgmanager.entity.OrganizationEntityPageable;
import com.rbkmoney.orgmanager.service.KeycloakService;
import com.rbkmoney.orgmanager.service.OrganizationService;
import com.rbkmoney.swag.organizations.api.UserApi;
import com.rbkmoney.swag.organizations.model.OrganizationJoinRequest;
import com.rbkmoney.swag.organizations.model.OrganizationMembership;
import com.rbkmoney.swag.organizations.model.OrganizationSearchResult;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.AccessToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final OrganizationService organizationService;
    private final KeycloakService keycloakService;

    @Override
    public ResponseEntity<Void> cancelOrgMembership(
            String xRequestID,
            String orgId) {
        AccessToken accessToken = keycloakService.getAccessToken();

        return organizationService.cancelOrgMembership(orgId, accessToken.getSubject(), accessToken.getEmail());
    }

    @Override
    public ResponseEntity<OrganizationMembership> inquireOrgMembership(
            String xRequestID,
            String orgId) {
        AccessToken accessToken = keycloakService.getAccessToken();

        return organizationService.getMembership(orgId, accessToken.getSubject(), accessToken.getEmail());
    }

    @Override
    public ResponseEntity<OrganizationMembership> joinOrg(
            String xRequestID,
            OrganizationJoinRequest body) {
        AccessToken accessToken = keycloakService.getAccessToken();

        return organizationService.joinOrganization(body.getInvitation(), accessToken.getSubject(), accessToken.getEmail());
    }

    @Override
    public ResponseEntity<OrganizationSearchResult> listOrgMembership(String xRequestID, Integer limit, String continuationToken) {
        OrganizationEntityPageable organizationEntityPageable;
        if (continuationToken == null) {
            organizationEntityPageable = organizationService.findAllOrganizations(limit);
        } else {
            organizationEntityPageable = organizationService.findAllOrganizations(continuationToken, limit);
        }
        OrganizationSearchResult organizationSearchResult = new OrganizationSearchResult();
        organizationSearchResult.setContinuationToken(organizationEntityPageable.getContinuationToken());
        organizationSearchResult.setResults(organizationEntityPageable.getOrganizations());

        return ResponseEntity.ok(organizationSearchResult);
    }

}
