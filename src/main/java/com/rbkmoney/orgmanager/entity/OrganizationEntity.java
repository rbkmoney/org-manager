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

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "organization")
public class OrganizationEntity implements Serializable {

    @Id
    private String id;

    private LocalDateTime createdAt;
    private String name;
    private String owner;
    private String metadata;
}