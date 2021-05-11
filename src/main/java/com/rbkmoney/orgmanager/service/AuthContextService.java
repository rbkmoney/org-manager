package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.context.v1.User;
import com.rbkmoney.bouncer.ctx.ContextFragment;
import com.rbkmoney.bouncer.ctx.ContextFragmentType;
import com.rbkmoney.orgmanagement.AuthContextProviderSrv;
import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthContextService implements AuthContextProviderSrv.Iface {

    private final UserService userService;

    @Override
    public ContextFragment getUserContext(String id) throws TException {
        com.rbkmoney.bouncer.context.v1.ContextFragment contextFragment =
                new com.rbkmoney.bouncer.context.v1.ContextFragment();
        User user = userService.findById(id);
        contextFragment.setUser(user);
        TSerializer byteSerializer = new TSerializer();
        return new ContextFragment()
                .setType(ContextFragmentType.v1_thrift_binary)
                .setContent(byteSerializer.serialize(contextFragment));
    }
}
