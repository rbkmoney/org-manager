package com.rbkmoney.orgmanager.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StackUtils {

    public static String getCallerMethodName() {
        Optional<StackWalker.StackFrame> callerFrame = StackWalker.getInstance()
                .walk(s -> s.skip(2).findFirst());
        if (callerFrame.isEmpty()) {
            throw new RuntimeException("Can't get caller method name");
        }
        return callerFrame.get().getMethodName();
    }

}
