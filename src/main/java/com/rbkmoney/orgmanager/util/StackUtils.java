package com.rbkmoney.orgmanager.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StackUtils {

    public static String getCallerMethodName() {
        Optional<StackWalker.StackFrame> callerFrame = StackWalker.getInstance()
                .walk(s -> s.skip(2).findFirst());
        if (callerFrame.isEmpty()) {
            log.error("Can't get caller method name");
            throw new RuntimeException();
        }
        return callerFrame.get().getMethodName();
    }

}
