plugins {
    alias(libs.plugins.convention.compose)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.library.composeUi)
                implementation(libs.coil.compose)
                implementation(libs.jetbrains.compose.material.icons.extended)
                implementation(libs.jetbrains.lifecycle.runtime.compose)
                implementation(libs.markdown.renderer.m3)
                api(libs.androidx.paging.compose)
                api(libs.jetbrains.compose.material3)
                api(libs.jetbrains.compose.material3.adaptive.navigation3)
                api(libs.kotlinx.datetime)
            }
        }

        androidHostTest {
            dependencies {
                implementation(libs.androidx.lifecycle.runtime.testing)
            }
        }
    }
}
