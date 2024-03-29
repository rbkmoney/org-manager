package com.rbkmoney.orgmanager.converter;

import com.rbkmoney.bouncer.context.v1.*;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BouncerContextConverter {

    public User toUser(MemberEntity member, Set<OrganizationEntity> organizations) {
        return new User()
                .setId(member == null ? null : member.getId())
                .setRealm(new Entity())
                .setEmail(member == null ? null : member.getEmail())
                .setOrgs(
                        organizations.stream()
                                .map(organizationEntity -> this.toOrganization(
                                        organizationEntity,
                                        member == null ? null : member.getRoles())
                                )
                                .collect(Collectors.toSet())
                );
    }

    public Organization toOrganization(OrganizationEntity entity,
                                       Set<MemberRoleEntity> roles) {
        return new Organization()
                .setId(entity.getId())
                .setOwner(new Entity().setId(entity.getOwner()))
                .setParty(new Entity().setId(entity.getId()))
                .setRoles(CollectionUtils.isEmpty(roles) ? null :
                        roles.stream()
                                .filter(memberRoleEntity -> memberRoleEntity.getOrganizationId().equals(entity.getId()))
                                .map(this::toOrgRole)
                                .collect(Collectors.toSet()));
    }

    public OrgRole toOrgRole(MemberRoleEntity entity) {
        return new OrgRole()
                .setId(entity.getRoleId())
                .setScope(new OrgRoleScope()
                        .setShop(new Entity().setId(entity.getResourceId())));

    }
}
