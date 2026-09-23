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
                implementation(projects.core.holidayDatabase.api)
                implementation(projects.library.room3)
                implementation(libs.androidx.room3.runtime)
            }
        }

        wasmJsMain {
            dependencies {
                implementation(libs.androidx.sqlite.web)
                implementation(libs.kotlinx.browser)
                implementation(
                    npm(
                        "sqlite-wasm-worker",
                        rootProject.layout.projectDirectory
                            .dir("core/database/impl/worker")
                            .asFile,
                    ),
                )
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
