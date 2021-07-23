package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.context.v1.Organization;
import com.rbkmoney.bouncer.context.v1.User;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.service.model.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final MemberService memberService;
    private final OrganizationService organizationService;

    @Transactional(readOnly = true)
    @Override
    public UserInfo findById(String id) {
        log.info("Find user with id {}", id);
        Optional<MemberEntity> user = memberService.findById(id);
        Set<OrganizationEntity> memberOrganizations = user
                .map(MemberEntity::getOrganizations)
                .orElseGet(HashSet::new);
        Set<OrganizationEntity> ownedOrganizations = organizationService.findByOwner(id);
        return new UserInfo(
                user.orElse(null),
                Stream.concat(memberOrganizations.stream(), ownedOrganizations.stream())
                        .collect(Collectors.toSet())
        );
    }
}
