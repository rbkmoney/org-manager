package com.rbkmoney.orgmanager.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StackUtils {

    private static final String CALLER_NOT_EXIST = "Caller method doesn't exist";

    public static String getCallerMethodName() {
        Optional<StackWalker.StackFrame> callerFrame = StackWalker.getInstance()
                .walk(s -> s.skip(2).findFirst());
        if (callerFrame.isEmpty()) {
            return CALLER_NOT_EXIST;
        }
        return callerFrame.get().getMethodName();
    }

}
