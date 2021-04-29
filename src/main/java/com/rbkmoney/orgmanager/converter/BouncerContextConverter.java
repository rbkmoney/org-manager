package com.rbkmoney.orgmanager.converter;

import com.rbkmoney.bouncer.context.v1.*;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BouncerContextConverter {

    public User toUser(MemberEntity member) {
        return new User()
                .setId(member.getId())
                .setRealm(new Entity())
                .setEmail(member.getEmail())
                .setOrgs(CollectionUtils.isEmpty(member.getOrganizations()) ? null :
                        member.getOrganizations()
                                .stream()
                                .map(organizationEntity -> this.toOrganization(organizationEntity, member.getRoles()))
                                .collect(Collectors.toSet()));
    }

    public Organization toOrganization(OrganizationEntity e,
                                       Set<MemberRoleEntity> roles) {
        return new Organization()
                .setId(e.getId())
                .setOwner(new Entity().setId(e.getOwner()))
                .setRoles(CollectionUtils.isEmpty(roles) ? null :
                        roles.stream()
                                .filter(memberRoleEntity -> memberRoleEntity.getOrganizationId().equals(e.getId()))
                                .map(this::toOrgRole)
                                .collect(Collectors.toSet()));
    }

    public OrgRole toOrgRole(MemberRoleEntity e) {
        return new OrgRole()
                .setId(e.getRoleId())
                .setScope(new OrgRoleScope()
                        .setShop(new Entity().setId(e.getResourceId())));

    }
}
