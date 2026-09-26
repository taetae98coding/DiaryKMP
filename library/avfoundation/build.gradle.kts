plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.primitive.kotlin)
    alias(libs.plugins.primitive.kotest)
}

kotlin {
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.library.objc)
                implementation(libs.jetbrains.compose.ui)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}
