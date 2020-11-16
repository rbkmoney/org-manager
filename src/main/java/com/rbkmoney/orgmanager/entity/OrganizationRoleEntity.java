package com.rbkmoney.orgmanager.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "organization_role")
public class OrganizationRoleEntity implements Serializable {

    @Id
    private String id;
    private String organizationId;
    private String roleId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "organization_role_to_scope",
            joinColumns = @JoinColumn(name = "organization_role_id"),
            inverseJoinColumns = @JoinColumn(name = "scope_id"))
    private Set<ScopeEntity> possibleScopes;

    private String name;
}
