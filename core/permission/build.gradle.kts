plugins {
    alias(libs.plugins.primitive.multiplatform.android.library)
    alias(libs.plugins.primitive.android.host.test)
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
