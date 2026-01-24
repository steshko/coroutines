package dev.steshko.playground.coroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.system.measureTimeMillis

fun main() {
    val future = CompletableFuture<String>()

    runBlocking {
        launch {
            delay(2500)
            future.complete("Hello World!!!")
        }
        measureTimeMillis {
            println(future.toSuspend() + " ${Thread.currentThread().name}")
        }.also(::println)
    }

}


suspend fun <T> CompletableFuture<T>.toSuspend(): T = suspendCoroutine { continuation ->
    this.thenAccept { continuation.resume(it) }
}
