package com.rbkmoney.orgmanager.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "role")
public class RoleEntity implements Serializable {

    @Id
    private String id;
    private String organizationId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "inviteeRoles")
    private Set<InvitationEntity> invitees = new HashSet<>();

    private String name;
//    private List<String> scopes; // TODO [a.romanov]: -> Scope
}