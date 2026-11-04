plugins {
    alias(libs.plugins.convention.compose.component)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.core.model)
                implementation(projects.library.composeUi)
                implementation(projects.library.kotlin)
                implementation(projects.library.kotlinxDatetime)
                implementation(libs.coil.compose)
                implementation(libs.jetbrains.lifecycle.runtime.compose)
            }
        }
    }
}
