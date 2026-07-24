plugins {
    alias(libs.plugins.primitive.multiplatform)
    alias(libs.plugins.primitive.koin)
    alias(libs.plugins.primitive.kotest)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.network.api)
                implementation(projects.core.supabase.api)
            }
        }

        jvmTest {
            dependencies {
                implementation(ktorLibs.client.contentNegotiation)
                implementation(ktorLibs.client.mock)
                implementation(ktorLibs.serialization.kotlinx.json)
            }
        }
    }
}
