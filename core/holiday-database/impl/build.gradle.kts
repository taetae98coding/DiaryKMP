@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.primitive.multiplatform.android.library)
    alias(libs.plugins.primitive.koin)
    alias(libs.plugins.primitive.kotest)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room3)
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

        nonWasmMain {
            dependencies {
                implementation(libs.androidx.sqlite.bundled)
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.androidx.room3.compiler)
    add("kspJvm", libs.androidx.room3.compiler)
    add("kspIosArm64", libs.androidx.room3.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room3.compiler)
    add("kspWasmJs", libs.androidx.room3.compiler)
}
