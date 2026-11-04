@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.convention.compose)
}

kotlin {
    swiftPMDependencies {
        iosMinimumDeploymentTarget.set(libs.versions.iosDeploymentTarget.get())

        swiftPackage(
            url = url("https://github.com/google/GoogleSignIn-iOS.git"),
            version = exact("10.0.0"),
            products = listOf(product("GoogleSignIn")),
        )
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.core.model)
                implementation(libs.jetbrains.compose.ui)
                implementation(libs.koin.annotations)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.kotlincrypto.hash.sha2)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.browser)
                implementation(libs.androidx.credentials.play.services.auth)
                implementation(libs.google.identity.googleid)
            }
        }

        androidHostTest {
            dependencies {
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
