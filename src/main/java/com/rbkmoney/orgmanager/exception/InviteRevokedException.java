package com.rbkmoney.orgmanager.exception;

import lombok.Getter;

@Getter
public class InviteRevokedException extends RuntimeException {

    private final String revokedAt;

    public InviteRevokedException(String revokedAt) {
        super();
        this.revokedAt = revokedAt;
    }

}
