package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.service.dto.BouncerContextDto;
import com.rbkmoney.orgmanager.util.StackUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RightServiceImpl implements RightService {

    private final BouncerService bouncerService;

    @Override
    public boolean haveRights() {
        String callerMethodName = StackUtils.getCallerMethodName();
        BouncerContextDto bouncerContext = BouncerContextDto.builder()
                .operationName(callerMethodName)
                .build();
        log.info("Check the user's rights to perform the operation {}", callerMethodName);
        return bouncerService.havePrivileges(bouncerContext);
    }

    @Override
    public boolean haveOrganizationRights(String orgId) {
        String callerMethodName = StackUtils.getCallerMethodName();
        BouncerContextDto bouncerContext = BouncerContextDto.builder()
                .operationName(callerMethodName)
                .organizationId(orgId)
                .build();
        log.info("Check the user's rights to perform the operation {} in organization {}", callerMethodName, orgId);
        return bouncerService.havePrivileges(bouncerContext);
    }

    @Override
    public boolean haveMemberRights(String orgId, String memberId) {
        String callerMethodName = StackUtils.getCallerMethodName();
        BouncerContextDto bouncerContext = BouncerContextDto.builder()
                .operationName(callerMethodName)
                .organizationId(orgId)
                .memberId(memberId)
                .build();
        log.info("Check the user's rights to perform the operation {} in organization {} with member {}",
                callerMethodName, orgId, memberId);
        return bouncerService.havePrivileges(bouncerContext);
    }
}
