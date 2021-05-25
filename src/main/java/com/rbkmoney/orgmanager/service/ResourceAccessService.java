package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.service.dto.ResourceDto;

public interface ResourceAccessService {

    void checkRights();

    void checkRights(ResourceDto resource);

}
