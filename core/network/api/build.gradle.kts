plugins {
    alias(libs.plugins.primitive.kmp)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.datetime)
                api(libs.kotlinx.io.core)

                implementation(libs.kotlinx.serialization.core)
            }
        }
    }
}
