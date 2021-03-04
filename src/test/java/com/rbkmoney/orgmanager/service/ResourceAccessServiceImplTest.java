package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.TestObjectFactory;
import com.rbkmoney.orgmanager.config.properties.AccessProperties;
import com.rbkmoney.orgmanager.exception.AccessDeniedException;
import com.rbkmoney.orgmanager.service.dto.BouncerContextDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ResourceAccessServiceImplTest {

    private AccessProperties accessProperties;
    @Mock
    private BouncerService bouncerService;

    private ResourceAccessService resourceAccessService;

    @BeforeEach
    void setUp() {
        accessProperties = new AccessProperties();
        accessProperties.setEnabled(true);
        resourceAccessService = new ResourceAccessServiceImpl(accessProperties, bouncerService);
    }

    @Test
    void checkNotEnabled() {
        accessProperties.setEnabled(false);

        assertDoesNotThrow(() -> resourceAccessService.checkRights());

        verify(bouncerService, times(0)).havePrivileges(any(BouncerContextDto.class));
    }

    @Test
    void checkRightsWithoutAccess() {
        when(bouncerService.havePrivileges(any(BouncerContextDto.class))).thenReturn(false);

        var exception = assertThrows(AccessDeniedException.class, () -> resourceAccessService.checkRights());

        assertThat(exception.getMessage(), containsString("No rights to perform"));
    }

    @Test
    void checkRightsSuccess() {
        when(bouncerService.havePrivileges(any(BouncerContextDto.class))).thenReturn(true);
        assertDoesNotThrow(() -> resourceAccessService.checkRights());
    }

    @Test
    void checkOrganizationNotEnabled() {
        accessProperties.setEnabled(false);
        var orgId = "test";

        assertDoesNotThrow(() -> resourceAccessService.checkOrganizationRights(orgId));

        verify(bouncerService, times(0)).havePrivileges(any(BouncerContextDto.class));
    }

    @Test
    void checkOrganizationRightsWithoutAccess() {
        String orgId = TestObjectFactory.randomString();
        when(bouncerService.havePrivileges(any(BouncerContextDto.class))).thenReturn(false);

        var exception = assertThrows(AccessDeniedException.class,
                () -> resourceAccessService.checkOrganizationRights(orgId));

        assertThat(exception.getMessage(), stringContainsInOrder("No rights to perform", orgId));
    }

    @Test
    void checkOrganizationRightsSuccess() {
        when(bouncerService.havePrivileges(any(BouncerContextDto.class))).thenReturn(true);
        assertDoesNotThrow(() -> resourceAccessService.checkOrganizationRights(anyString()));
    }

    @Test
    void checkMemberNotEnabled() {
        accessProperties.setEnabled(false);
        var orgId = "test";
        var memberId = "test";

        assertDoesNotThrow(() -> resourceAccessService.checkMemberRights(orgId, memberId));

        verify(bouncerService, times(0)).havePrivileges(any(BouncerContextDto.class));
    }

    @Test
    void checkMemberRightsWithoutAccess() {
        String orgId = TestObjectFactory.randomString();
        String memberId = TestObjectFactory.randomString();
        when(bouncerService.havePrivileges(any(BouncerContextDto.class))).thenReturn(false);

        var exception = assertThrows(AccessDeniedException.class,
                () -> resourceAccessService.checkMemberRights(orgId, memberId));

        assertThat(exception.getMessage(), stringContainsInOrder("No rights to perform", orgId, memberId));
    }

    @Test
    void checkMemberRightsSuccess() {
        String orgId = TestObjectFactory.randomString();
        String memberId = TestObjectFactory.randomString();
        when(bouncerService.havePrivileges(any(BouncerContextDto.class))).thenReturn(true);
        assertDoesNotThrow(() -> resourceAccessService.checkMemberRights(orgId, memberId));
    }
}