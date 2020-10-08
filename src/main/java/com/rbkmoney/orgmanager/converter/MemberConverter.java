package com.rbkmoney.orgmanager.converter;

import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.swag.organizations.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class MemberConverter {

    private final MemberRoleConverter memberRoleConverter;

    public Member toDomain(MemberEntity entity) {
        return new Member()
                .id(entity.getId())
                .userEmail(entity.getEmail())
                .roles(entity.getRoles().stream()
                        .map(memberRoleConverter::toDomain)
                        .collect(toSet()));
    }
}
