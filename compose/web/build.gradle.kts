plugins {
    alias(libs.plugins.primitive.multiplatform.android.library)
    alias(libs.plugins.primitive.compose)
    alias(libs.plugins.primitive.compose.preview)
    alias(libs.plugins.primitive.compose.test)
    alias(libs.plugins.primitive.kotest)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.core)
                api(projects.core.model)
            }
        }

        jvmMain {
            dependencies {
                implementation(projects.library.webkit)
            }
        }
    }
}
