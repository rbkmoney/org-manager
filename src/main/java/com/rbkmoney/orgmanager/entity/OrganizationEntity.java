package com.rbkmoney.orgmanager.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "organization")
public class OrganizationEntity implements Serializable {

    @Id
    private String id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "organizations")
    private Set<MemberEntity> members = new HashSet<>();

    private LocalDateTime createdAt;
    private String name;
    private String owner;
    private String metadata;
}
