package dev.steshko.coroutines

import kotlinx.coroutines.suspendCancellableCoroutine

suspend fun mySuspendFunWrapper(): String =
    suspendCancellableCoroutine { continuation ->
        MySuspendFunctionWrapper.mySuspendFun(continuation);
    }