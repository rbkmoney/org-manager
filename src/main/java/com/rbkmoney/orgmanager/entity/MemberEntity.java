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
@Table(name = "member")
public class MemberEntity implements Serializable {

    @Id
    private String id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "member_to_member_role",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "member_role_id"))
    private Set<MemberRoleEntity> roles;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "members")
    private Set<OrganizationEntity> organizations = new HashSet<>();

    private String email;
}
