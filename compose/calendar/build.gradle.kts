plugins {
    alias(libs.plugins.primitive.multiplatform.android.library)
    alias(libs.plugins.primitive.compose)
    alias(libs.plugins.primitive.compose.resources)
    alias(libs.plugins.primitive.compose.preview)
    alias(libs.plugins.primitive.compose.test)
    alias(libs.plugins.primitive.kotest)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.core)
                implementation(projects.library.composeUi)
                implementation(projects.library.kotlinxDatetime)
            }
        }
    }
}
