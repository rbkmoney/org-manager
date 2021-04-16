package com.rbkmoney.orgmanager.entity;

import lombok.*;

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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MemberRoleEntity implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    private String id;
    private String organizationId;
    private String roleId;
    private String scopeId;
    private String resourceId;
    private boolean active = true;
}
