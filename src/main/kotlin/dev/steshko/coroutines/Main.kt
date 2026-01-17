package dev.steshko.coroutines

import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println(mySuspendFunWrapper())
}