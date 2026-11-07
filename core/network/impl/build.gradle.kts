plugins {
    alias(libs.plugins.convention.core.impl)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.supabase.api)
                implementation(projects.library.ktor)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }

        iosMain {
            dependencies {
                implementation(projects.library.coroutines)
            }
        }

        jvmTest {
            dependencies {
                implementation(ktorLibs.client.contentNegotiation)
                implementation(ktorLibs.client.mock)
                implementation(ktorLibs.serialization.kotlinx.json)
                implementation(projects.logger.crashlytics.api)
            }
        }
    }
}
