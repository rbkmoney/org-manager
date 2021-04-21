package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.context.v1.User;
import com.rbkmoney.orgmanagement.UserNotFound;
import com.rbkmoney.orgmanager.TestObjectFactory;
import com.rbkmoney.orgmanager.repository.AbstractRepositoryTest;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceImplTest extends AbstractRepositoryTest {

    @Autowired
    private UserService userService;

    @Test
    void findByIdWithoutMember() {
        String memberId = TestObjectFactory.randomString();

        assertThrows(UserNotFound.class, () -> userService.findById(memberId));
    }

    @Test
    void findByIdSuccess() throws TException {
        String memberId = TestObjectFactory.randomString();
        var member = TestObjectFactory.testMemberEntity(memberId);
        memberRepository.save(member);

        User actualUser = userService.findById(memberId);

        assertEquals(member.getId(), actualUser.getId());
        assertEquals(member.getEmail(), actualUser.getEmail());

    }

}