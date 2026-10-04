@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.primitive.kmp)
    alias(libs.plugins.primitive.android.library)
    alias(libs.plugins.primitive.kotest)
}

kotlin {
    swiftPMDependencies {
        iosMinimumDeploymentTarget.set(libs.versions.iosDeploymentTarget.get())

        swiftPackage(
            url = url("https://github.com/firebase/firebase-ios-sdk.git"),
            version = exact(libs.versions.firebaseIos.get()),
            products = listOf(product("FirebaseCrashlytics")),
        )
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.logger.crashlytics.api)
            }
        }

        androidMain {
            dependencies {
                implementation(project.dependencies.platform(libs.firebase.bom))
                implementation(libs.firebase.crashlytics)
            }
        }
    }
}
