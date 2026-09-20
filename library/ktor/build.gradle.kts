plugins {
    alias(libs.plugins.primitive.multiplatform.android.library)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(ktorLibs.client.core)
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
