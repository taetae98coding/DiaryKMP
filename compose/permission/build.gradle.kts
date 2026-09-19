plugins {
    alias(libs.plugins.primitive.multiplatform.android.library)
    alias(libs.plugins.primitive.compose)
    alias(libs.plugins.primitive.compose.test)
    alias(libs.plugins.primitive.kotest)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.core.permission)
                implementation(libs.jetbrains.compose.ui)
                implementation(libs.jetbrains.lifecycle.runtime.compose)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.activity.compose)
            }
        }

        androidHostTest {
            dependencies {
                implementation(libs.androidx.lifecycle.runtime.testing)
                implementation(libs.jetbrains.compose.material3)
            }
        }
    }
}
