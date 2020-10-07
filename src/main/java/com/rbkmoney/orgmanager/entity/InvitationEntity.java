package com.rbkmoney.orgmanager.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "invitation")
public class InvitationEntity implements Serializable {

    @Id
    private String id;
    private String organizationId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "invitee_role",
            joinColumns = @JoinColumn(name = "invitation_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleEntity> inviteeRoles;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String acceptToken;
    private String metadata;
    private String inviteeContactType; // TODO [a.romanov]: enum
    private String inviteeContactEmail;
    private String status; // TODO [a.romanov]: enum
    private String revocationReason;
}
