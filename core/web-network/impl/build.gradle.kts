plugins {
    alias(libs.plugins.primitive.multiplatform)
    alias(libs.plugins.primitive.koin)
    alias(libs.plugins.primitive.kotest)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.webNetwork.api)

                implementation(ktorLibs.client.core)
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
