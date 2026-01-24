plugins {
    val kotlinVersion = "2.3.0"
    kotlin("jvm") version kotlinVersion apply false
    kotlin("plugin.spring") version kotlinVersion apply false
    kotlin("plugin.jpa") version kotlinVersion apply false
    id("org.springframework.boot") version "4.0.2" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
    group = "dev.steshko.playground"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    plugins.withType<JavaPlugin> {
        configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(25))
            }
        }
    }
}