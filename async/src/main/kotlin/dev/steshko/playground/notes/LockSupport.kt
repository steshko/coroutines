package dev.steshko.playground.notes

import java.util.concurrent.locks.LockSupport
import kotlin.concurrent.thread
import kotlin.time.measureTime

/**
 * Example of using LockSupport
 *
 * LockSupport is the lowest-level thread blocking primitive available in Java
 * (java.util.concurrent.locks.LockSupport). It provides park/unpark operations
 * that most higher-level concurrency utilities are built upon.
 *
 * Implementation varies by OS:
 *   - Linux: futex (fast userspace mutex)
 *   - macOS/BSD: pthreads (pthread_cond_wait/signal)
 *   - Windows: WaitOnAddress or Event objects
 *
 * Key advantages over wait()/notify():
 *   - No synchronized block required
 *   - unpark() before park() doesn't get lost (permit model)
 *   - Target specific threads directly
 *
 * Permit model:
 *   LockSupport uses a single binary permit (0 or 1) per thread.
 *   - unpark() sets permit to 1
 *   - park() consumes permit (sets to 0), or blocks if no permit
 *   - If unpark() is called before park(), the permit is saved and
 *     the subsequent park() returns immediately without blocking
 *   - Multiple unpark() calls don't stack: calling unpark() twice then
 *     park() twice will still block on the second park(), because
 *     permits don't accumulate beyond 1. Use a Semaphore if you need
 *     counting permits.
 *
 * The hierarchy:
 *   OS primitives → Unsafe.park → LockSupport → AQS → ReentrantLock, Semaphore, etc.
 */
fun main() {
    var sharedState: String? = null

    val t = thread {
        println("Begin Processing")
        LockSupport.park() // blocks until permit available, then consumes it
        println("End Processing: $sharedState")
    }

    thread(isDaemon = true) {
        println("Generating shared State")
        Thread.sleep(1000)
        sharedState = "STATE_VALUE"
        println("Generated Shared State $sharedState")
        LockSupport.unpark(t) // gives permit to thread t, waking it if parked

    }
    Object().wait()

    thread {
        println("Waiting until specific time...")
        measureTime {
            LockSupport.parkUntil(System.currentTimeMillis() + 2000)  // 2 seconds from now
        }.also {
            println("Unblocked after $it")
        }
    }
}