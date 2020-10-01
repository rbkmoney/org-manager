package com.rbkmoney.orgmanager.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "invitation")
public class InvitationEntity implements Serializable {

    @Id
    private String id;
    private String orgId; // TODO [a.romanov]: -> Organization
    
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String acceptToken;
    private String metadata;

    private String inviteeContactType;
    private String inviteeContactEmail;
    private List<String> inviteeRoles; // TODO [a.romanov]: -> Role
}
