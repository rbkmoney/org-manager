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
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
            name = "invitation_to_member_role",
            joinColumns = @JoinColumn(name = "invitation_id"),
            inverseJoinColumns = @JoinColumn(name = "member_role_id"))
    private Set<MemberRoleEntity> inviteeRoles;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime revokedAt;
    private String acceptToken;
    private String metadata;
    private String inviteeContactType; // TODO [a.romanov]: enum
    private String inviteeContactEmail;
    private String status; // TODO [a.romanov]: enum
    private String revocationReason;
    private String acceptedMemberId;

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

}
