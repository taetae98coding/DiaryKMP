plugins {
    alias(libs.plugins.primitive.kmp)
    alias(libs.plugins.primitive.android.library)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(ktorLibs.client.core)
                api(ktorLibs.serialization.kotlinx.json)

                implementation(ktorLibs.client.contentNegotiation)
            }
        }

        androidJvmMain {
            dependencies {
                implementation(ktorLibs.client.okhttp)
            }
        }

        iosMain {
            dependencies {
                implementation(ktorLibs.client.darwin)
            }
        }

        wasmJsMain {
            dependencies {
                implementation(ktorLibs.client.js)
            }
        }
    }
}
