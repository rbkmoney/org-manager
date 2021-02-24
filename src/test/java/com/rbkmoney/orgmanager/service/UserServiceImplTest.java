package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.context.v1.User;
import com.rbkmoney.orgmanagement.UserNotFound;
import com.rbkmoney.orgmanager.TestObjectFactory;
import com.rbkmoney.orgmanager.converter.MemberConverter;
import com.rbkmoney.orgmanager.repository.MemberRepository;
import org.apache.thrift.TException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberConverter memberConverter;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(memberRepository, memberConverter);
    }


    @Test
    void findByIdWithoutMember() {
        String memberId = TestObjectFactory.randomString();
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThrows(UserNotFound.class, () -> userService.findById(memberId));
    }

    @Test
    void findByIdSuccess() throws TException {
        String memberId = TestObjectFactory.randomString();
        var member = TestObjectFactory.testMemberEntity(memberId);
        var user = TestObjectFactory.testUser();
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(memberConverter.toThrift(member)).thenReturn(user);

        User actualUser = userService.findById(memberId);

        assertEquals(user, actualUser);
    }

}