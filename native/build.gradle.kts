plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    macosArm64 {
        binaries {
            executable {
                entryPoint = "dev.steshko.playground.native.main"
            }
        }
    }
}