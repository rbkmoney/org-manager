package com.rbkmoney.orgmanager.service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleDto {

    private String roleId;
    private String scopeResourceId;

}
