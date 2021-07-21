package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.ctx.ContextFragmentType;
import com.rbkmoney.orgmanager.TestObjectFactory;
import com.rbkmoney.orgmanager.converter.BouncerContextConverter;
import com.rbkmoney.orgmanager.service.model.UserInfo;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthContextServiceTest {

    private final BouncerContextConverter bouncerConverter = new BouncerContextConverter();
    private final TDeserializer byteDeserializer = new TDeserializer();
    @Mock
    private UserService userService;
    private AuthContextService service;

    @BeforeEach
    void setUp() {
        service = new AuthContextService(userService, bouncerConverter);
    }

    @Test
    void testUserContext() throws TException {
        var id = TestObjectFactory.randomString();
        var member = TestObjectFactory.testMemberEntity(id);
        var organization = TestObjectFactory.buildOrganization(member);
        var userInfo = new UserInfo(member, Set.of(organization));
        when(userService.findById(id)).thenReturn(userInfo);

        var userContext = service.getUserContext(id);

        verify(userService, times(1)).findById(id);
        var contextFragment = new com.rbkmoney.bouncer.context.v1.ContextFragment();
        byteDeserializer.deserialize(contextFragment, userContext.getContent());

        assertEquals(member.getId(), contextFragment.getUser().getId());
        assertEquals(ContextFragmentType.v1_thrift_binary, userContext.getType());
    }
}
