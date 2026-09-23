plugins {
    alias(libs.plugins.primitive.kmp)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.androidx.room3.common)
                implementation(libs.kotlinx.serialization.core)
                api(libs.androidx.paging.common)
                api(libs.kotlinx.datetime)
            }
        }
    }
}
