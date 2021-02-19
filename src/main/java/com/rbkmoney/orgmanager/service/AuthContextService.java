package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.context.v1.User;
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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthContextService implements AuthContextProviderSrv.Iface {

    private final MemberRepository memberRepository;
    private final MemberConverter memberConverter;

    @Override
    @Transactional(readOnly = true)
    public ContextFragment getUserContext(String id) throws TException {
        com.rbkmoney.bouncer.context.v1.ContextFragment contextFragment =
                new com.rbkmoney.bouncer.context.v1.ContextFragment();
        User user = getUser(id);
        contextFragment.setUser(user);
        TSerializer tSerializer = new TSerializer();
        return new ContextFragment()
                .setType(ContextFragmentType.v1_thrift_binary)
                .setContent(tSerializer.serialize(contextFragment));
    }

    private User getUser(String id) throws UserNotFound {
        MemberEntity member = memberRepository.findById(id)
                .orElseThrow(UserNotFound::new);
        return memberConverter.toThrift(member);
    }
}
