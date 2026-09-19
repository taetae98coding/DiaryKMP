@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.primitive.multiplatform.android.library)
    alias(libs.plugins.primitive.kotest)
}

kotlin {
    swiftPMDependencies {
        iosMinimumDeploymentTarget.set("26.5")

        swiftPackage(
            url = url("https://github.com/firebase/firebase-ios-sdk.git"),
            version = exact("12.19.1"),
            products = listOf(product("FirebaseAnalytics")),
        )
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.logger.analytics.api)
            }
        }

        androidMain {
            dependencies {
                implementation(project.dependencies.platform(libs.firebase.bom))
                implementation(libs.firebase.analytics)
            }
        }
    }
}
