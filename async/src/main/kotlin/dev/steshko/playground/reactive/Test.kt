package dev.steshko.playground.reactive

import reactor.core.publisher.Mono

fun main() {
    val mono = Mono.just("mono")

    mono.subscribe {
        println(it)
    }

    mono.subscribe {
        println(it)
    }
}