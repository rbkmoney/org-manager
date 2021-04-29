package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.entity.MemberEntity;

import java.util.Optional;

public interface MemberService {

    Optional<MemberEntity> findById(String id);

}
