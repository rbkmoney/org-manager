package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.ctx.ContextFragment;
import com.rbkmoney.bouncer.ctx.ContextFragmentType;
import com.rbkmoney.orgmanagement.AuthContextProviderSrv;
import com.rbkmoney.orgmanagement.UserNotFound;
import com.rbkmoney.orgmanager.converter.MemberConverter;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthContextService implements AuthContextProviderSrv.Iface {

    private final MemberRepository memberRepository;
    private final MemberConverter memberConverter;

    @Override
    public ContextFragment getUserContext(String id) throws UserNotFound, TException {

        Optional<MemberEntity> entity = memberRepository.findById(id);
        if (entity.isEmpty()) {
            throw new UserNotFound();
        }
        MemberEntity member = entity.get();
        TSerializer tSerializer = new TSerializer();
        return new ContextFragment()
                .setType(ContextFragmentType.v1_thrift_binary)
                .setContent(tSerializer.serialize(memberConverter.toThrift(member)));
    }
}
