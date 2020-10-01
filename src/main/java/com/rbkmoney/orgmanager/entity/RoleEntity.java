package com.rbkmoney.orgmanager.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "role")
public class RoleEntity implements Serializable {

    @Id
    private String id;
    private String orgId; // TODO [a.romanov]: -> Organization

    private String name;
    private List<String> scopes; // TODO [a.romanov]: -> Scope
}