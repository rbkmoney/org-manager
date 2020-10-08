package com.rbkmoney.orgmanager.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "member_role")
public class MemberRoleEntity implements Serializable {

    @Id
    private String id;
    private String organizationId;
    private String roleId;
    private String scopeId;
    private String resourceId;
}
