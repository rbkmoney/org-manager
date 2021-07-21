package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.context.v1.User;
import com.rbkmoney.bouncer.ctx.ContextFragment;
import com.rbkmoney.bouncer.ctx.ContextFragmentType;
import com.rbkmoney.orgmanagement.AuthContextProviderSrv;
import com.rbkmoney.orgmanager.converter.BouncerContextConverter;
import com.rbkmoney.orgmanager.service.model.UserInfo;
import com.rbkmoney.woody.api.trace.ContextUtils;
import com.rbkmoney.woody.api.trace.context.metadata.user.UserIdentityEmailExtensionKit;
import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthContextService implements AuthContextProviderSrv.Iface {

    private final UserService userService;
    private final BouncerContextConverter bouncerConverter;

    @Override
    public ContextFragment getUserContext(String id) throws TException {
        com.rbkmoney.bouncer.context.v1.ContextFragment contextFragment =
                new com.rbkmoney.bouncer.context.v1.ContextFragment();
        contextFragment.setUser(getUser(id));
        TSerializer byteSerializer = new TSerializer();
        return new ContextFragment()
                .setType(ContextFragmentType.v1_thrift_binary)
                .setContent(byteSerializer.serialize(contextFragment));
    }

    private User getUser(String id) {
        UserInfo userInfo = userService.findById(id);
        User bouncerUser = bouncerConverter.toUser(userInfo.getMember(), userInfo.getOrganizations());
        if (userInfo.getMember() == null) {
            bouncerUser.setId(id);
            bouncerUser.setEmail(
                    ContextUtils.getCustomMetadataValue(UserIdentityEmailExtensionKit.INSTANCE.getExtension())
            );
        }
        return bouncerUser;
    }
}
