plugins {
    alias(libs.plugins.primitive.kmp)
    alias(libs.plugins.primitive.android.library)
    alias(libs.plugins.primitive.koin)
    alias(libs.plugins.primitive.kotest)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.integrity.api)
            }
        }

        androidMain {
            dependencies {
                implementation(project.dependencies.platform(libs.firebase.bom))
                implementation(libs.firebase.common)
                implementation(libs.google.play.integrity)
                implementation(libs.kotlinx.coroutines.play.services)
            }
        }
    }
}
