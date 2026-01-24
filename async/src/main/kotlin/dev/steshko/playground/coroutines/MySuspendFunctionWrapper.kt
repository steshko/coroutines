package dev.steshko.playground.coroutines

import kotlinx.coroutines.suspendCancellableCoroutine

suspend fun mySuspendFunWrapper(): String =
    suspendCancellableCoroutine { continuation ->
        MySuspendFunctionWrapper.mySuspendFun(continuation);
    }