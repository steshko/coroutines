package dev.steshko.playground.coroutines

import kotlinx.coroutines.delay
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.system.measureTimeMillis
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED

fun main() {
    val x: suspend (Long) -> Unit = ::delay
    x as (Long, Continuation<Unit>) -> Unit

    val continuation = MyMainContinuationImpl<Unit>(EmptyCoroutineContext)
    measureTimeMillis {
        myTwoDelays(continuation)
        continuation.latch.await()
    }.also(::println)


}

class MyMainContinuationImpl<T>(override val context: CoroutineContext) : Continuation<T> {
    override fun resumeWith(result: Result<T>) {
        latch.countDown()
    }

    val latch = CountDownLatch(1)
}

fun myTwoDelays(continuation: Continuation<*>): Any {
    class MyTwoDelaysContinuation(
        override val context: CoroutineContext,
        val parentContinuation: Continuation<*>
    ) : Continuation<Unit> {
        var label = 0
        override fun resumeWith(result: Result<Unit>) {
            myTwoDelays(this)
        }
    }

    val continuationToUse = when (continuation) {
        is MyTwoDelaysContinuation -> continuation
        else -> MyTwoDelaysContinuation(EmptyCoroutineContext, continuation)
    }

    val x: suspend (Long) -> Unit = ::delay
    x as (Long, Continuation<Unit>) -> Unit
    when (continuationToUse.label) {
        0 -> {
            continuationToUse.label = 1
            x(1000, continuationToUse)
        }
        1 -> {
            continuationToUse.label = 2
            x(1500L, continuationToUse)
        }
        else -> (continuationToUse.parentContinuation as Continuation<Unit>).resumeWith(Result.success(Unit))
    }
    return COROUTINE_SUSPENDED
}

