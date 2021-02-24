package com.rbkmoney.orgmanager.converter;

import com.rbkmoney.bouncer.context.v1.Entity;
import com.rbkmoney.bouncer.context.v1.User;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.swag.organizations.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class MemberConverter {

    private final MemberRoleConverter memberRoleConverter;
    private final OrganizationConverter organizationConverter;

    public Member toDomain(MemberEntity entity) {
        return new Member()
                .id(entity.getId())
                .userEmail(entity.getEmail())
                .roles(entity.getRoles().stream()
                        .map(memberRoleConverter::toDomain)
                        .collect(toSet()));
    }

    public User toThrift(MemberEntity member) {
        return new User()
                .setId(member.getId())
                .setRealm(new Entity()) // TODO мб тут "externel"?
                .setEmail(member.getEmail())
                .setOrgs(member.getOrganizations() == null ? null :
                        member.getOrganizations()
                                .stream()
                                .map(organizationConverter::toThrift)
                                .collect(Collectors.toSet()));
    }
}
