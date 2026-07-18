plugins {
    alias(libs.plugins.primitive.multiplatform)
    alias(libs.plugins.primitive.kotest)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.coroutines.core)
            }
        }
    }
}
