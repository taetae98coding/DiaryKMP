plugins {
    alias(libs.plugins.primitive.kmp)
    alias(libs.plugins.primitive.android.library)
    alias(libs.plugins.primitive.kotest)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(ktorLibs.client.core)
                api(ktorLibs.serialization.kotlinx.json)

                implementation(ktorLibs.client.contentNegotiation)
                implementation(projects.logger.crashlytics.api)
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

        jvmTest {
            dependencies {
                implementation(ktorLibs.client.mock)
            }
        }

        wasmJsMain {
            dependencies {
                implementation(ktorLibs.client.js)
            }
        }
    }
}
