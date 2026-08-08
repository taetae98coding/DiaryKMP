plugins {
    alias(libs.plugins.primitive.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.datetime)

                implementation(libs.kotlinx.serialization.core)
            }
        }
    }
}
