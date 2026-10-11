plugins {
    alias(libs.plugins.convention.core.impl)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.supabase.api)
            }
        }

        iosMain {
            dependencies {
                implementation(projects.library.coroutines)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.core.testing)
                implementation(ktorLibs.client.contentNegotiation)
                implementation(ktorLibs.client.mock)
                implementation(ktorLibs.serialization.kotlinx.json)
            }
        }
    }
}
