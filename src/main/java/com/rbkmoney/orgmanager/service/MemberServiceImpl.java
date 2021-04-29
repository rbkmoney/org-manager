package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    @Override
    public Optional<MemberEntity> findById(String id) {
        return memberRepository.findById(id);
    }
}
