package com.rbkmoney.orgmanager.service.model;

import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.annotation.Nullable;

import java.util.Set;

@Data
@AllArgsConstructor
public class UserInfo {
    @Nullable
    private MemberEntity member;
    private Set<OrganizationEntity> organizations;
}
