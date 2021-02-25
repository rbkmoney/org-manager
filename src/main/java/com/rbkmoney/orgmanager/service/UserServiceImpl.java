package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.context.v1.User;
import com.rbkmoney.orgmanagement.UserNotFound;
import com.rbkmoney.orgmanager.converter.MemberConverter;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final MemberRepository memberRepository;
    private final MemberConverter memberConverter;

    @Transactional(readOnly = true)
    @Override
    public User findById(String id) throws UserNotFound {
        MemberEntity member = memberRepository.findById(id)
                .orElseThrow(UserNotFound::new);
        return memberConverter.toThrift(member);
    }
}
