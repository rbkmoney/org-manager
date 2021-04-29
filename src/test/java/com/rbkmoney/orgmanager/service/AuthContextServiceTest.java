package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.context.v1.User;
import com.rbkmoney.bouncer.ctx.ContextFragment;
import com.rbkmoney.bouncer.ctx.ContextFragmentType;
import com.rbkmoney.orgmanager.TestObjectFactory;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthContextServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthContextService service;

    private final TDeserializer tDeserializer = new TDeserializer();

    @Test
    void testUserContext() throws TException {
        String id = TestObjectFactory.randomString();
        User user = TestObjectFactory.testUser();
        when(userService.findById(id)).thenReturn(user);

        ContextFragment userContext = service.getUserContext(id);

        verify(userService, times(1)).findById(id);
        com.rbkmoney.bouncer.context.v1.ContextFragment contextFragment =
                new com.rbkmoney.bouncer.context.v1.ContextFragment();
        tDeserializer.deserialize(contextFragment, userContext.getContent());


        assertEquals(user.getId(), contextFragment.getUser().getId());
        assertEquals(ContextFragmentType.v1_thrift_binary, userContext.getType());
    }
}
