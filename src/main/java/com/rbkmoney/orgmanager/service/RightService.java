package com.rbkmoney.orgmanager.service;

public interface RightService {

    boolean haveRights();

    boolean haveOrganizationRights(String orgId);

    boolean haveMemberRights(String orgId, String memberId);

}
