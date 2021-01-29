package com.rbkmoney.orgmanager.controller.error;

import lombok.Getter;

@Getter
public class InviteExpiredException extends RuntimeException {

    private String expiredAt;

    public InviteExpiredException(String expiredAt) {
        super();
        this.expiredAt = expiredAt;
    }

    public InviteExpiredException(String expiredAt, Throwable cause) {
        super(cause);
        this.expiredAt = expiredAt;
    }

}
