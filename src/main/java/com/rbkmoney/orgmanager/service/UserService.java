package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.context.v1.User;
import com.rbkmoney.orgmanagement.UserNotFound;

public interface UserService {

    User findById(String id) throws UserNotFound;

}
