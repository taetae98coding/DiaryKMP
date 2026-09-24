@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.primitive.kmp)
    alias(libs.plugins.primitive.android.library)
    alias(libs.plugins.primitive.koin)
    alias(libs.plugins.primitive.kotest)
}

kotlin {
    swiftPMDependencies {
        iosMinimumDeploymentTarget.set("26.5")
        swiftPackage(
            url = url("https://github.com/firebase/firebase-ios-sdk.git"),
            version = exact("12.19.1"),
            products = listOf(product("FirebaseMessaging")),
        )
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.fcm.api)
            }
        }

        iosMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        androidMain {
            dependencies {
                implementation(project.dependencies.platform(libs.firebase.bom))
                implementation(libs.firebase.messaging)
                implementation(libs.kotlinx.coroutines.play.services)
            }
        }
    }
}
