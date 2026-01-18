package dev.steshko.coroutines;

import static kotlinx.coroutines.DelayKt.delay;

public class Main {
    void main() throws Throwable {
        System.out.println("Starting suspend function call");
        var s = MyBlockingContinuation.myRunBlocking(continuation -> {
            delay(1000L, continuation);
            continuation.getResult();
            MySuspendFunctionWrapper.mySuspendFun(continuation);
            return continuation.getResult();
        });

        System.out.println(s);
    }
}
