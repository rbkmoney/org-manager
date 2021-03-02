package com.rbkmoney.orgmanager.service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BouncerContextDto {

    private String operationName;
    private String organizationId;
    private String memberId;
    private RoleDto role;

}
