package com.rbkmoney.orgmanager.service;

public interface ResourceAccessService {

    void checkRights();

    void checkOrganizationRights(String orgId);

    void checkMemberRights(String orgId, String memberId);

}
