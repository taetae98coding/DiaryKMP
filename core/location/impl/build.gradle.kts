plugins {
    alias(libs.plugins.convention.core.impl)
    alias(libs.plugins.primitive.android.host.test)
}

kotlin {
    sourceSets {
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
