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
                implementation(projects.core.file.api)
                implementation(projects.domain.playlist)
                implementation(projects.logger.console.api)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }
    }
}
