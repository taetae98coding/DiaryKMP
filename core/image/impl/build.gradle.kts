plugins {
    alias(libs.plugins.primitive.multiplatform.android.library)
    alias(libs.plugins.primitive.koin)
    alias(libs.plugins.primitive.kotest)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.image.api)
            }
        }

        androidJvmMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        wasmJsMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}
