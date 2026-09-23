@file:OptIn(ExperimentalWasmDsl::class)

import io.github.taetae98coding.diary.buildlogic.localProperties
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.primitive.kotlin)
    alias(libs.plugins.primitive.compose)
}

kotlin {
    wasmJs {
        outputModuleName = "diary"
        browser {
            commonWebpackConfig {
                outputFileName = "diary.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.app.shared)
                implementation(libs.jetbrains.compose.ui)
            }
        }
    }
}

tasks.named<ProcessResources>("wasmJsProcessResources") {
    val buildKonfigFlavor = providers.gradleProperty("buildkonfig.flavor").orElse("dev").get()
    val naverMapNcpKeyIdProperty = "$buildKonfigFlavor.naverMapNcpKeyId"
    val googleMapApiKeyProperty = "$buildKonfigFlavor.web.googleMapApiKey"
    val naverMapNcpKeyId =
        requireNotNull(localProperties().getProperty(naverMapNcpKeyIdProperty)) {
            "$naverMapNcpKeyIdProperty is missing from local.properties"
        }
    val googleMapApiKey =
        requireNotNull(localProperties().getProperty(googleMapApiKeyProperty)) {
            "$googleMapApiKeyProperty is missing from local.properties"
        }

    inputs.property("naverMapNcpKeyId", naverMapNcpKeyId)
    inputs.property("googleMapApiKey", googleMapApiKey)

    filesMatching("index.html") {
        expand(
            "naverMapNcpKeyId" to naverMapNcpKeyId,
            "googleMapApiKey" to googleMapApiKey,
        )
    }
}
