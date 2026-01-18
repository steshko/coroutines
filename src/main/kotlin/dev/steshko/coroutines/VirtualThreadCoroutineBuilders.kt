package dev.steshko.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> runOnVirtual(block: () -> T): T = suspendCancellableCoroutine { continuation ->
    val virtualThread = Thread.startVirtualThread {
        try {
            continuation.resume(block())
        } catch (e: Throwable) {
            if (!continuation.isCancelled || e !is InterruptedException) {
                continuation.resumeWithException(e)
            }
        }
    }

    continuation.invokeOnCancellation {
        virtualThread.interrupt()
    }
}

fun <T> CoroutineScope.launchOnVirtual(block: () -> T): Job = launch {
    runOnVirtual(block)
}

fun <T> CoroutineScope.asyncOnVirtual(block: () -> T): Deferred<T> = async {
    runOnVirtual(block)
}