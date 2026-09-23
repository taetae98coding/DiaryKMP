@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.primitive.kmp)
}

kotlin {
    wasmJs {
        useEsModules()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.androidx.room3.common)

                api(libs.kotlinx.datetime)
            }
        }

        wasmJsMain {
            dependencies {
                api(libs.androidx.sqlite.web)
                implementation(libs.kotlinx.browser)
                implementation(npm("sqlite-wasm-worker", layout.projectDirectory.dir("worker").asFile))
            }
        }
    }
}
