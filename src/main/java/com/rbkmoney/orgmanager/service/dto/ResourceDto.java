package com.rbkmoney.orgmanager.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceDto {

    private String orgId;
    private String memberId;
    private String memberRoleId;
    private String roleId;
    private String scopeResourceId;
    private String invitationId;
    private String email;
    private String invitationToken;

}
