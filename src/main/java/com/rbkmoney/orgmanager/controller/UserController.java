package com.rbkmoney.orgmanager.controller;

import com.rbkmoney.swag.organizations.api.UserApi;
import com.rbkmoney.swag.organizations.model.InlineResponse200;
import com.rbkmoney.swag.organizations.model.OrganizationJoinRequest;
import com.rbkmoney.swag.organizations.model.OrganizationMembership;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController implements UserApi {

    @Override
    public ResponseEntity<Void> cancelOrgMembership(
            String xRequestID,
            String orgId) {
        throw new UnsupportedOperationException(); // TODO [a.romanov]: impl
    }

    @Override
    public ResponseEntity<OrganizationMembership> inquireOrgMembership(
            String xRequestID,
            String orgId) {
        throw new UnsupportedOperationException(); // TODO [a.romanov]: impl
    }

    @Override
    public ResponseEntity<OrganizationMembership> joinOrg(
            String xRequestID,
            OrganizationJoinRequest body) {
        throw new UnsupportedOperationException(); // TODO [a.romanov]: impl
    }

    @Override
    public ResponseEntity<InlineResponse200> listOrgMembership(String xRequestID) {
        throw new UnsupportedOperationException(); // TODO [a.romanov]: impl
    }
}
