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
                implementation(projects.domain.setting)
                implementation(projects.library.ktor)
                implementation(projects.logger.console.api)

                implementation(libs.kotlinx.io.core)
            }
        }

        jvmMain {
            dependencies {
                implementation(ktorLibs.server.core)
                implementation(ktorLibs.server.cio)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.core.testing)

                implementation(ktorLibs.client.mock)
                implementation(ktorLibs.server.testHost)
            }
        }
    }
}
