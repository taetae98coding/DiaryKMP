@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.convention.compose.component)
}

kotlin {
    swiftPMDependencies {
        iosMinimumDeploymentTarget.set(libs.versions.iosDeploymentTarget.get())

        swiftPackage(
            url = url("https://github.com/navermaps/SPM-NMapsMap.git"),
            version = exact(libs.versions.naverMap.get()),
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
                implementation(projects.compose.permission)
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

        iosMain {
            dependencies {
                implementation(libs.koin.annotations)
                implementation(libs.koin.compose.viewmodel)
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.koin.compose.viewmodel)
                implementation(projects.library.webkit)
                implementation(libs.kotlinx.serialization.json)
                implementation(ktorLibs.server.cio)
            }
        }
    }
}
