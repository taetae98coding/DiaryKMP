@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.convention.core.impl)
    alias(libs.plugins.primitive.android.host.test)
}

kotlin {
    android {
        androidResources {
            enable = true
        }
    }

    swiftPMDependencies {
        iosMinimumDeploymentTarget.set(libs.versions.iosDeploymentTarget.get())
        swiftPackage(
            url = url("https://github.com/firebase/firebase-ios-sdk.git"),
            version = exact(libs.versions.firebaseIos.get()),
            products = listOf(product("FirebaseMessaging")),
        )
    }

    sourceSets {
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
                implementation(libs.androidx.startup.runtime)
            }
        }
    }
}
