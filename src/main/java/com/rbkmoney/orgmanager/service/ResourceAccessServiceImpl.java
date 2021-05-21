package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.config.properties.AccessProperties;
import com.rbkmoney.orgmanager.exception.AccessDeniedException;
import com.rbkmoney.orgmanager.service.dto.BouncerContextDto;
import com.rbkmoney.orgmanager.service.dto.InvitationDto;
import com.rbkmoney.orgmanager.service.dto.ResourceDto;
import com.rbkmoney.orgmanager.service.dto.RoleDto;
import com.rbkmoney.orgmanager.util.StackUtils;
import com.rbkmoney.swag.organizations.model.MemberRole;
import com.rbkmoney.swag.organizations.model.MemberRoleScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceAccessServiceImpl implements ResourceAccessService {

    private final AccessProperties accessProperties;
    private final BouncerService bouncerService;
    private final OrganizationService organizationService;
    private final MemberRoleService memberRoleService;

    @Override
    public void checkRights() {
        if (isCheckAccessDisabled()) {
            return;
        }
        String callerMethodName = StackUtils.getCallerMethodName();
        BouncerContextDto bouncerContext = BouncerContextDto.builder()
                .operationName(callerMethodName)
                .build();
        callBouncer(callerMethodName, bouncerContext);
    }

    @Override
    public void checkRights(ResourceDto resource) {
        if (isCheckAccessDisabled()) {
            return;
        }
        String callerMethodName = StackUtils.getCallerMethodName();
        BouncerContextDto bouncerContext = buildBouncerContextDto(resource, callerMethodName);
        if (Objects.nonNull(resource.getInvitationToken())) {
            log.info("Get organization by invitation token");
            String orgId = organizationService.getOrgIdByInvitationToken(resource.getInvitationToken());
            bouncerContext.setOrganizationId(orgId);
        }
        if (Objects.nonNull(resource.getMemberRoleId())) {
            String memberRoleId = resource.getMemberRoleId();
            log.info("Get member role by id {}", memberRoleId);
            MemberRole memberRole = memberRoleService.findById(memberRoleId);
            MemberRoleScope scope = memberRole.getScope();
            bouncerContext.setRole(RoleDto.builder()
                    .scopeResourceId(Objects.nonNull(scope) ? scope.getResourceId() : null)
                    .roleId(memberRole.getRoleId().getValue())
                    .build());
        }
        callBouncer(callerMethodName, bouncerContext);
    }

    private BouncerContextDto buildBouncerContextDto(ResourceDto resource, String callerMethodName) {
        RoleDto role = RoleDto.builder()
                .roleId(resource.getRoleId())
                .scopeResourceId(resource.getScopeResourceId())
                .build();
        InvitationDto invitation = InvitationDto.builder()
                .invitationId(resource.getInvitationId())
                .email(resource.getEmail())
                .build();
        return BouncerContextDto.builder()
                .operationName(callerMethodName)
                .organizationId(resource.getOrgId())
                .memberId(resource.getMemberId())
                .invitation(invitation)
                .role(role)
                .build();
    }

    private boolean isCheckAccessDisabled() {
        return Boolean.FALSE.equals(accessProperties.getEnabled());
    }

    private void callBouncer(String callerMethodName, BouncerContextDto bouncerContext) {
        log.info("Check the user's rights to perform the operation {}", callerMethodName);
        if (!bouncerService.havePrivileges(bouncerContext)) {
            throw new AccessDeniedException(
                    String.format("No rights to perform %s", callerMethodName));
        }
    }

}
