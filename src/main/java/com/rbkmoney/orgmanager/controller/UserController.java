package com.rbkmoney.orgmanager.controller;

import com.rbkmoney.swag.organizations.api.UserApi;
import com.rbkmoney.swag.organizations.model.InlineResponse2003;
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
        return null;
    }

    @Override
    public ResponseEntity<OrganizationMembership> inquireOrgMembership(
            String xRequestID,
            String orgId) {
        return null;
    }

    @Override
    public ResponseEntity<OrganizationMembership> joinOrg(
            String xRequestID,
            OrganizationJoinRequest body) {
        return null;
    }

    @Override
    public ResponseEntity<InlineResponse2003> listOrgMembership(String xRequestID) {
        return null;
    }
}
