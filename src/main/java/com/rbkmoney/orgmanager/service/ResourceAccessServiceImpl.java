package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.config.properties.AccessProperties;
import com.rbkmoney.orgmanager.exception.AccessDeniedException;
import com.rbkmoney.orgmanager.service.dto.BouncerContextDto;
import com.rbkmoney.orgmanager.util.StackUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceAccessServiceImpl implements ResourceAccessService {

    private final AccessProperties accessProperties;
    private final BouncerService bouncerService;

    @Override
    public void checkRights() {
        if (!accessProperties.getEnabled()) {
            return;
        }
        String callerMethodName = StackUtils.getCallerMethodName();
        BouncerContextDto bouncerContext = BouncerContextDto.builder()
                .operationName(callerMethodName)
                .build();
        log.info("Check the user's rights to perform the operation {}", callerMethodName);
        if (!bouncerService.havePrivileges(bouncerContext)) {
            throw new AccessDeniedException(
                    String.format("No rights to perform %s", callerMethodName));
        }
    }

    @Override
    public void checkOrganizationRights(String orgId) {
        if (!accessProperties.getEnabled()) {
            return;
        }
        String callerMethodName = StackUtils.getCallerMethodName();
        BouncerContextDto bouncerContext = BouncerContextDto.builder()
                .operationName(callerMethodName)
                .organizationId(orgId)
                .build();
        log.info("Check the user's rights to perform the operation {} in organization {}", callerMethodName, orgId);
        if (!bouncerService.havePrivileges(bouncerContext)) {
            throw new AccessDeniedException(
                    String.format("No rights to perform %s in %s", callerMethodName, orgId));
        }
    }

    @Override
    public void checkMemberRights(String orgId, String memberId) {
        if (!accessProperties.getEnabled()) {
            return;
        }
        String callerMethodName = StackUtils.getCallerMethodName();
        BouncerContextDto bouncerContext = BouncerContextDto.builder()
                .operationName(callerMethodName)
                .organizationId(orgId)
                .memberId(memberId)
                .build();
        log.info("Check the user's rights to perform the operation {} in organization {} with member {}",
                callerMethodName, orgId, memberId);
        if (!bouncerService.havePrivileges(bouncerContext)) {
            throw new AccessDeniedException(
                    String.format("No rights to perform %s in %s with %s", callerMethodName, orgId, memberId));
        }
    }
}
