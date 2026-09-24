@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    swiftPMDependencies {
        iosMinimumDeploymentTarget.set("26.5")

        swiftPackage(
            url = url("https://github.com/google/GoogleSignIn-iOS.git"),
            version = exact("10.0.0"),
            products = listOf(product("GoogleSignIn")),
        )
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.domain.account)
                implementation(projects.feature.login.api)
                implementation(libs.kotlincrypto.hash.sha2)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.browser)
                implementation(libs.androidx.credentials.play.services.auth)
                implementation(libs.google.identity.googleid)
            }
        }

        androidHostTest {
            dependencies {
                implementation(libs.androidx.lifecycle.runtime.testing)
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.google.oauth.client.jetty)
            }
        }
    }
}
