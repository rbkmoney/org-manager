package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.service.dto.BouncerContextDto;

public interface BouncerService {

    boolean havePrivileges(BouncerContextDto bouncerContext);
}
