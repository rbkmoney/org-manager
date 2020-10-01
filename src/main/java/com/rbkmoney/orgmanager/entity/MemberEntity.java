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
@Table(name = "member")
public class MemberEntity implements Serializable {

    @Id
    private String id;
    private List<String> organizations; // TODO [a.romanov]: -> Organization

    private String email;
    private List<String> roles; // TODO [a.romanov]: -> Role
}