@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.primitive.multiplatform.android.library)
    alias(libs.plugins.primitive.compose)
    alias(libs.plugins.primitive.compose.resources)
    alias(libs.plugins.primitive.compose.preview)
    alias(libs.plugins.primitive.compose.test)
    alias(libs.plugins.primitive.kotest)
}

kotlin {
    swiftPMDependencies {
        iosMinimumDeploymentTarget.set("26.5")

        swiftPackage(
            url = url("https://github.com/navermaps/SPM-NMapsMap.git"),
            version = exact("3.24.0"),
            products = listOf(product("NMapsMap")),
        )

        swiftPackage(
            url = url("https://github.com/googlemaps/ios-maps-sdk.git"),
            version = exact("11.1.0"),
            products = listOf(product("GoogleMaps")),
        )
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.core)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.google.maps.compose)
                implementation(libs.google.play.services.location)
                implementation(libs.naver.map)
                implementation(libs.jetbrains.lifecycle.runtime.compose)
            }
        }

        jvmMain {
            dependencies {
                implementation(projects.library.webkit)
                implementation(libs.kotlinx.serialization.json)
                implementation(ktorLibs.server.cio)
            }
        }
    }
}
