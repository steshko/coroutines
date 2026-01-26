package dev.steshko.playground.coroutines

import java.util.concurrent.LinkedBlockingQueue
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.startCoroutine

fun <T> myRunBlocking(block: suspend () -> T): T {
    val queue = LinkedBlockingQueue<Runnable>()

    val blockingContinuation = object : Continuation<T> {
        @Volatile var result: Result<T>? = null
        override val context: CoroutineContext = object : ContinuationInterceptor {
            override val key: CoroutineContext.Key<*> = ContinuationInterceptor

            override fun <S> interceptContinuation(continuation: Continuation<S>): Continuation<S> {
                return object : Continuation<S> {
                    override val context: CoroutineContext = continuation.context
                    override fun resumeWith(result: Result<S>) {
                        queue.put {
                            continuation.resumeWith(result)
                        }
                    }
                }
            }
        }

        override fun resumeWith(result: Result<T>) {
            this.result = result
        }
    }

    block.startCoroutine(blockingContinuation)

    while (blockingContinuation.result == null) {
        queue.take().run()
    }

    return blockingContinuation.result!!.getOrThrow()
}