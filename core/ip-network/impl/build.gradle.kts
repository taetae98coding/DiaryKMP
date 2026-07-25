plugins {
    alias(libs.plugins.primitive.multiplatform)
    alias(libs.plugins.primitive.koin)
    alias(libs.plugins.primitive.kotest)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.ipNetwork.api)

                implementation(ktorLibs.client.contentNegotiation)
                implementation(ktorLibs.serialization.kotlinx.json)
            }
        }

        jvmTest {
            dependencies {
                implementation(ktorLibs.client.mock)
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

        jvmMain {
            dependencies {
                implementation(ktorLibs.client.okhttp)
            }
        }
    }
}
