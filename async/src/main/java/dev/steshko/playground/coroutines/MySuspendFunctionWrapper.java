package dev.steshko.playground.coroutines;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import org.jetbrains.annotations.NotNull;

class MySuspendFunctionWrapper {
    private static class MySuspendStateMachine implements Continuation<String> {
        int label = 0;
        private final Continuation<?> parentContinuation;
        MySuspendStateMachine(Continuation<?> parentContinuation) {
            this.parentContinuation = parentContinuation;
        }

        @Override
        public void resumeWith(@NotNull Object result) {
            var finalResult = mySuspendFun(this);
            if (finalResult != IntrinsicsKt.getCOROUTINE_SUSPENDED())
                parentContinuation.resumeWith(finalResult);
        }

        @Override
        public @NotNull CoroutineContext getContext() {
            return parentContinuation.getContext();
        }
    }


    /**
     * Kotlin equivalent:
     * <pre>
     * suspend fun mySuspendFun(): String {
     *     delay(5000)
     *     return "Hello World"
     * }
     * </pre>
     *
     * @param continuation the continuation
     * @return "Hello World" or COROUTINE_SUSPENDED
     */
    protected static Object mySuspendFun(Continuation<? super String> continuation) {
        MySuspendStateMachine sm;
        if (continuation instanceof MySuspendStateMachine stateMachine) {
            // This is a resume
            sm = stateMachine;
        } else {
            // This is a fresh call - create our state machine (sm)
             sm = new MySuspendStateMachine(continuation);
        }
        switch (sm.label) {
            case 0:
                sm.label = 1;

                Thread.startVirtualThread(() -> {
                    try {
                        Thread.sleep(5000); //5 second delay
                        sm.resumeWith(Unit.INSTANCE);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                return IntrinsicsKt.getCOROUTINE_SUSPENDED();
            case 1:
                return "Hello World";
            default:
                throw new IllegalStateException("Unknown State: " + sm.label);
        }
    }
}
