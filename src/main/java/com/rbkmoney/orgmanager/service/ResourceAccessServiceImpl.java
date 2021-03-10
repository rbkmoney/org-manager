package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.config.properties.AccessProperties;
import com.rbkmoney.orgmanager.exception.AccessDeniedException;
import com.rbkmoney.orgmanager.service.dto.BouncerContextDto;
import com.rbkmoney.orgmanager.service.dto.InvitationDto;
import com.rbkmoney.orgmanager.service.dto.RoleDto;
import com.rbkmoney.orgmanager.util.StackUtils;
import com.rbkmoney.swag.organizations.model.InvitationRequest;
import com.rbkmoney.swag.organizations.model.MemberRole;
import com.rbkmoney.swag.organizations.model.OrganizationJoinRequest;
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

    @Override
    public void checkRights() {
        if (isCheckAccessDisabled()) {
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

    private boolean isCheckAccessDisabled() {
        return Boolean.FALSE.equals(accessProperties.getEnabled());
    }

    @Override
    public void checkOrganizationRights(String orgId) {
        if (isCheckAccessDisabled()) {
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
    public void checkOrganizationRights(OrganizationJoinRequest request) {
        if (isCheckAccessDisabled()) {
            return;
        }
        log.info("Get organization by invitation token");
        String orgId = organizationService.getOrgIdByInvitationToken(request.getInvitation());
        checkOrganizationRights(orgId);
    }

    @Override
    public void checkMemberRights(String orgId, String memberId) {
        if (isCheckAccessDisabled()) {
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
                    String.format("No rights to perform %s in %s with member %s", callerMethodName, orgId, memberId));
        }
    }

    @Override
    public void checkRoleRights(String orgId, MemberRole memberRole) {
        if (isCheckAccessDisabled()) {
            return;
        }
        String callerMethodName = StackUtils.getCallerMethodName();
        BouncerContextDto bouncerContext =
                buildRoleBouncerContextDto(orgId, memberRole, callerMethodName);
        log.info("Check the user's rights to perform the operation {} in organization {} with role {}",
                callerMethodName, orgId, memberRole.getRoleId().getValue());
        if (!bouncerService.havePrivileges(bouncerContext)) {
            throw new AccessDeniedException(
                    String.format("No rights to perform %s in %s with role %s", callerMethodName, orgId,
                            memberRole.getRoleId().getValue()));
        }
    }

    private BouncerContextDto buildRoleBouncerContextDto(String orgId, MemberRole memberRole, String callerMethodName) {
        RoleDto role = RoleDto.builder()
                .roleId(memberRole.getRoleId().getValue())
                .scopeResourceId(Objects.nonNull(memberRole.getScope()) ? memberRole.getScope().getResourceId() : null)
                .build();
        return BouncerContextDto.builder()
                .operationName(callerMethodName)
                .organizationId(orgId)
                .role(role)
                .build();
    }

    @Override
    public void checkMemberRoleRights(String orgId, String memberId, MemberRole memberRole) {
        if (isCheckAccessDisabled()) {
            return;
        }
        String callerMethodName = StackUtils.getCallerMethodName();
        BouncerContextDto bouncerContext = buildRoleBouncerContextDto(orgId, memberRole, callerMethodName);
        bouncerContext.setMemberId(memberId);
        log.info("Check the user's rights to perform the operation {} in organization {} with member {} and role {}",
                callerMethodName, orgId, memberId, memberRole.getRoleId().getValue());
        if (!bouncerService.havePrivileges(bouncerContext)) {
            throw new AccessDeniedException(
                    String.format("No rights to perform %s in %s with member %s and role %s", callerMethodName, orgId,
                            memberId,
                            memberRole.getRoleId().getValue()));
        }
    }

    @Override
    public void checkInvitationRights(String orgId, InvitationRequest invitationRequest) {
        if (isCheckAccessDisabled()) {
            return;
        }
        String callerMethodName = StackUtils.getCallerMethodName();
        InvitationDto invitation = InvitationDto.builder()
                .email(invitationRequest.getInvitee().getContact().getEmail())
                .build();
        BouncerContextDto bouncerContext = BouncerContextDto.builder()
                .operationName(callerMethodName)
                .organizationId(orgId)
                .invitation(invitation)
                .build();
        log.info("Check the user's rights to perform the operation {} in organization {} with email {}",
                callerMethodName, orgId, invitation.getEmail());
        if (!bouncerService.havePrivileges(bouncerContext)) {
            throw new AccessDeniedException(
                    String.format("No rights to perform %s in %s with email %s", callerMethodName, orgId,
                            invitation.getEmail()));
        }
    }
}
