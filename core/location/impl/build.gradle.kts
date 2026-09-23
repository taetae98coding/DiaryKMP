plugins {
    alias(libs.plugins.primitive.kmp)
    alias(libs.plugins.primitive.android.library)
    alias(libs.plugins.primitive.android.host.test)
    alias(libs.plugins.primitive.koin)
    alias(libs.plugins.primitive.kotest)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.location.api)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.google.play.services.location)
                implementation(libs.kotlinx.coroutines.play.services)
            }
        }

        iosMain {
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
