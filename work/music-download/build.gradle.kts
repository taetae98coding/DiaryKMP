plugins {
    alias(libs.plugins.convention.work)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.file.api)
                implementation(projects.domain.playlist)
                implementation(projects.domain.setting)
                implementation(projects.library.coroutines)
                implementation(projects.library.ktor)
                implementation(projects.logger.console.api)
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
