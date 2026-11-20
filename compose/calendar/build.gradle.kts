plugins {
    alias(libs.plugins.convention.compose.component)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.icon)
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
