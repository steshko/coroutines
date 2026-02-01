package dev.steshko.playground.spring.webflux

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringWebfluxMain

fun main(args: Array<String>) {
    runApplication<SpringWebfluxMain>(*args)
}
