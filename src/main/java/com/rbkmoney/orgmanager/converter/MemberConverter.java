package com.rbkmoney.orgmanager.converter;

import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.orgmanager.service.dto.MemberDto;
import com.rbkmoney.orgmanager.service.dto.MemberWithRoleDto;
import com.rbkmoney.swag.organizations.model.Member;
import com.rbkmoney.swag.organizations.model.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

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
                        .collect(toList()));
    }

    public Member toDomain(MemberEntity entity, List<MemberRoleEntity> roles) {
        return new Member()
                .id(entity.getId())
                .userEmail(entity.getEmail())
                .roles(roles.stream()
                        .map(memberRoleConverter::toDomain)
                        .collect(toList()));
    }

    public List<Member> toDomain(List<MemberWithRoleDto> memberWithRoleDtos) {
        Map<MemberDto, List<MemberWithRoleDto>> rolesByMember = memberWithRoleDtos.stream()
                .collect(groupingBy(
                        memberWithRoleDto -> new MemberDto(memberWithRoleDto.getId(), memberWithRoleDto.getEmail())));
        return rolesByMember.entrySet().stream()
                .map(this::toMember)
                .collect(toList());
    }

    private Member toMember(Map.Entry<MemberDto, List<MemberWithRoleDto>> roleWithMember) {
        Member member = new Member();
        MemberDto memberDto = roleWithMember.getKey();
        member.setId(memberDto.getId());
        member.setUserEmail(memberDto.getEmail());
        List<MemberRole> roles = roleWithMember.getValue().stream()
                .map(memberRoleConverter::toDomain)
                .collect(toList());
        member.setRoles(roles);
        return member;
    }
}
