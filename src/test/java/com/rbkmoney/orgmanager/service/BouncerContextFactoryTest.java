package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.ctx.ContextFragment;
import com.rbkmoney.bouncer.ctx.ContextFragmentType;
import com.rbkmoney.bouncer.decisions.Context;
import com.rbkmoney.orgmanagement.UserNotFound;
import com.rbkmoney.orgmanager.TestObjectFactory;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.rbkmoney.orgmanager.service.BouncerContextFactory.CONTEXT_FRAGMENT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BouncerContextFactoryTest {

    @Mock
    private UserService userService;

    @Mock
    private KeycloakService keycloakService;

    @InjectMocks
    private BouncerContextFactory bouncerContextFactory;

    private final TDeserializer tDeserializer = new TDeserializer();


    @Test
    void buildContextSuccess() throws TException {
        var user = TestObjectFactory.testUser();
        var token = TestObjectFactory.testToken();
        when(userService.findById(token.getSubject())).thenReturn(user);
        when(keycloakService.getAccessToken()).thenReturn(token);

        Context context = bouncerContextFactory.buildContext();

        ContextFragment fragment = context.getFragments().get(CONTEXT_FRAGMENT_ID);
        com.rbkmoney.bouncer.context.v1.ContextFragment contextFragment =
                new com.rbkmoney.bouncer.context.v1.ContextFragment();
        tDeserializer.deserialize(contextFragment, fragment.getContent());

        assertEquals(ContextFragmentType.v1_thrift_binary, fragment.getType());
        assertEquals(token.getId(), contextFragment.getAuth().getToken().getId());
        assertEquals(user.getId(), contextFragment.getUser().getId());

    }

    @Test
    void buildContextWithoutUser() throws TException {
        var user = TestObjectFactory.testUser();
        var token = TestObjectFactory.testToken();
        when(userService.findById(token.getSubject())).thenThrow(new UserNotFound());
        when(keycloakService.getAccessToken()).thenReturn(token);

        assertThrows(UserNotFound.class, () -> bouncerContextFactory.buildContext());

    }

}