@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.primitive.kmp)
    alias(libs.plugins.primitive.android.library)
    alias(libs.plugins.primitive.koin)
    alias(libs.plugins.primitive.kotest)
    alias(libs.plugins.primitive.room)
}

kotlin {
    wasmJs {
        useEsModules()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.database.api)
                implementation(projects.library.room3)
                implementation(libs.androidx.room3.paging)
                implementation(libs.kotlinx.serialization.json)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.androidx.paging.testing)
                implementation(libs.androidx.room3.testing)
            }
        }

        wasmJsMain {
            dependencies {
                implementation(libs.androidx.sqlite.web)
                implementation(libs.kotlinx.browser)
                implementation(npm("sqlite-wasm-worker", layout.projectDirectory.dir("worker").asFile))
            }
        }

        jvmMain {
            dependencies {
                implementation(projects.library.applicationSupport)
            }
        }

        nonWasmMain {
            dependencies {
                implementation(libs.androidx.sqlite.bundled)
            }
        }
    }
}
