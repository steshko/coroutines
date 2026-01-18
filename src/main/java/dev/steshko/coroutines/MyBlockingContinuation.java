package dev.steshko.coroutines;

import kotlin.Result;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CountDownLatch;

public class MyBlockingContinuation<T> implements Continuation<T> {
    private CountDownLatch latch = new CountDownLatch(1);
    private T finalResult;
    private Throwable error;

    @Override
    public void resumeWith(@NotNull Object result) {
        if (result instanceof Result.Failure failure) {
            this.error = failure.exception;
        } else {
            this.finalResult = (T) result;
        }
        latch.countDown();
    }

    @Override
    public @NotNull CoroutineContext getContext() {
        return EmptyCoroutineContext.INSTANCE;
    }

    public T getResult() throws Throwable {
        latch.await();
        var finalResult = this.finalResult;
        var error = this.error;
        resetLatch();
        if (error != null) throw error;

        return finalResult;
    }

    public void resetLatch() {
        this.finalResult = null;
        this.error = null;
        this.latch = new CountDownLatch(1);
    }

    public static <T> T myRunBlocking(MySuspendBlock<T> block) throws Throwable {
        var continuation = new MyBlockingContinuation<T>();
        Object result = block.execute(continuation);
        if (result == IntrinsicsKt.getCOROUTINE_SUSPENDED()) {
            // Function suspended, wait for it to complete
            var finalResult = continuation.getResult();
            continuation.resetLatch();
            return finalResult;
        } else {
            // Completed immediately
            return (T) result;
        }
    }
}
