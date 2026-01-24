package dev.steshko.playground.coroutines;

@FunctionalInterface
public interface MySuspendBlock<T> {
    Object execute(MyBlockingContinuation<? super T> continuation) throws Throwable;
}
