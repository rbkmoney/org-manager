package com.rbkmoney.orgmanager.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

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
