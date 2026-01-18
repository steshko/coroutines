package dev.steshko.coroutines;

@FunctionalInterface
public interface MySuspendBlock<T> {
    Object execute(MyBlockingContinuation<? super T> continuation) throws Throwable;
}
