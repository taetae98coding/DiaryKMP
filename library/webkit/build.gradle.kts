plugins {
    alias(libs.plugins.primitive.jvm)
    alias(libs.plugins.primitive.kotest)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.jetbrains.compose.ui)
            }
        }
    }
}
