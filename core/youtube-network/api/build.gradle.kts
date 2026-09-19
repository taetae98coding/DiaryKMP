plugins {
    alias(libs.plugins.primitive.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization.core)
            }
        }
    }
}
