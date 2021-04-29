package com.rbkmoney.orgmanager.service;

import com.rbkmoney.bouncer.context.v1.User;

public interface UserService {

    User findById(String id);

}
