plugins {
    kotlin("jvm")
}

dependencies {
    implementation("io.netty:netty-all:4.2.9.Final")
    runtimeOnly("io.netty:netty-transport-native-io_uring:4.2.9.Final")
    implementation("io.projectreactor:reactor-core:3.8.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}