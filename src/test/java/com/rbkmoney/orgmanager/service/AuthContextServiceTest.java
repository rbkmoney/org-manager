package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.context.v1.User;
import com.rbkmoney.bouncer.ctx.ContextFragment;
import com.rbkmoney.bouncer.ctx.ContextFragmentType;
import com.rbkmoney.orgmanagement.UserNotFound;
import com.rbkmoney.orgmanager.converter.MemberConverter;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.repository.MemberRepository;
import org.apache.thrift.TException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthContextServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private MemberConverter memberConverter;

    @InjectMocks
    private AuthContextService service;

    @Test
    public void testUserContext() throws TException {
        String id = "1";
        when(memberRepository.findById(id)).thenReturn(Optional.of(new MemberEntity()));
        when(memberConverter.toThrift(any())).thenReturn(new User());

        ContextFragment userContext = service.getUserContext(id);

        verify(memberRepository, times(1)).findById(id);
        verify(memberConverter, times(1)).toThrift(any());
        assertEquals(ContextFragmentType.v1_thrift_binary, userContext.getType());
    }

    @Test(expected = UserNotFound.class)
    public void testUserNotFound() throws TException {
        String id = "1";
        when(memberRepository.findById(id)).thenReturn(Optional.empty() );
        service.getUserContext(id);
    }
}
