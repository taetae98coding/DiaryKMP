plugins {
    alias(libs.plugins.primitive.multiplatform)
    alias(libs.plugins.primitive.kotest)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization.core)
                api(libs.kotlinx.coroutines.core)
            }
        }
    }
}
